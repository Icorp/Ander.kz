package ander.kz.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import java.util.Objects;

import ander.kz.CardDetailActivity;
import ander.kz.Menu;
import ander.kz.R;
import ander.kz.Search;
import ander.kz.SetttingsActivity;
import ander.kz.models.Model;
import ander.kz.viewholder.CardViewHolder;

public abstract class CardListFragment extends Fragment {
    public static final String TAG = "TAGS";
    public Activity mActivity;
    public DatabaseReference mDatabase;
    public FirebaseRecyclerAdapter<Model, CardViewHolder> mAdapter;
    public RecyclerView mRecycler;

    //For Sorting
    LinearLayoutManager mLayoutManager;

    //For saving sort setting
    SharedPreferences mSharedPref;


    public CardListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_cards, container, false);
        mRecycler = rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        //Sorting
        mSharedPref = getActivity().getSharedPreferences("SortSetting", getActivity().MODE_PRIVATE);
        String mSorting = mSharedPref.getString("Sort", "Жаңа"); //where if no setting is selected

        if (mSorting.equals("Жаңа")) {
            //This will load the items from button means newest first
            mLayoutManager = new LinearLayoutManager(getActivity());
            mLayoutManager.setReverseLayout(true);
            mLayoutManager.setStackFromEnd(true);
        } else if (mSorting.equals("Ескі")) {
            //This will load the items from bottom means oldest first
            mLayoutManager = new LinearLayoutManager(getActivity());
            mLayoutManager.setReverseLayout(false);
            mLayoutManager.setStackFromEnd(false);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        mActivity = getActivity();

        //Sorting
        mSharedPref = mActivity.getSharedPreferences("SortSetting", mActivity.MODE_PRIVATE);
        String mSorting = mSharedPref.getString("Sort", "newest"); //where if no setting is selected


        if (mSorting.equals("newest")) {
            //This will load the items from button means newest first
            mLayoutManager = new LinearLayoutManager(mActivity);
            mLayoutManager.setReverseLayout(true);
            mLayoutManager.setStackFromEnd(true);
        } else if (mSorting.equals("oldest")) {
            //This will load the items from bottom means oldest first
            mLayoutManager = new LinearLayoutManager(mActivity);
            mLayoutManager.setReverseLayout(false);
            mLayoutManager.setStackFromEnd(false);
        }


        final Dialog mDialog = new Dialog(mActivity, R.style.NewDialog);
        mDialog.addContentView(
                new ProgressBar(mActivity),
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        );
        mDialog.setCancelable(true);
        mDialog.show();

        mRecycler.setLayoutManager(mLayoutManager);

        Query cardsQuery = getQuery(mDatabase);

        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(cardsQuery, Model.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Model, CardViewHolder>(options) {
            @Override
            public void onBindViewHolder(@NonNull CardViewHolder viewHolder, int position, @NonNull final Model model) {
                final DatabaseReference cardRef = getRef(position);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), CardDetailActivity.class);
                        intent.putExtra(CardDetailActivity.EXTRA_CARD_KEY, cardRef.getKey());
                        startActivity(intent);
                    }
                });

                if (!TextUtils.isEmpty(model.kzSongytblink))
                    viewHolder.mImage.setImageResource(R.drawable.on_line_logo);
                else viewHolder.mImage.setImageResource(R.drawable.off_line_logo);

                if (model.stars.containsKey(getUid())) {
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
                } else {
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                }

                viewHolder.bindToCard(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        // Need to write to both places the post is stored
                        DatabaseReference globalCardRef = mDatabase.child("songsDB").child(Objects.requireNonNull(cardRef.getKey()));

                        // Run two transactions
                        onStarClicked(globalCardRef);
                    }
                });
            }

            @NonNull
            @Override
            public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new CardViewHolder(inflater.inflate(R.layout.item_card, viewGroup, false));
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                mDialog.dismiss();
            }
        };

        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    public void showSortDialog() {
        //options to display in dialog
        String[] sortOptions = {"Жаңа", "Ескі"};

        //Create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Sort by")
                .setIcon(R.drawable.ic_action_sort)
                .setItems(sortOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //The 'which' argument contains the index position of the selected item
                        // 0 means "Newest" and 1 means "oldest"

                        if (which == 0) {
                            //Sort by newest
                            //Edit our shared prefences
                            SharedPreferences.Editor editor = mSharedPref.edit();
                            editor.putString("Sort", "Жаңа"); // where 'Sort' is key & 'newest' is value
                            editor.apply();//apply / save the value in our shared preferences
                            getActivity().recreate(); //Restart acitivity to take effect
                        } else if (which == 1) {
                            //Sort by oldest
                            //Edit our shared prefences
                            SharedPreferences.Editor editor = mSharedPref.edit();
                            editor.putString("Sort", "Ескі"); // where 'Sort' is key & 'newest' is value
                            editor.apply();//apply / save the value in our shared preferences
                            getActivity().recreate(); //Restart acitivity to take effect
                        }
                    }
                });
        builder.show();
    }

    //Liked
    public void onStarClicked(DatabaseReference cardRef) {
        cardRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Model p = mutableData.getValue(Model.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.stars.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    p.starCount = p.starCount - 1;
                    p.stars.remove(getUid());
                } else {
                    // Star the post and add self to stars
                    p.starCount = p.starCount + 1;
                    p.stars.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d("postTransaction", "onComplete:" + dataSnapshot.getKey());
            }
        });
    }

    //GetID
    public String getUid() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    @Override
    //Menu
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent searchIntent = new Intent(getActivity(), Search.class);
                startActivity(searchIntent);
                return true;
            case R.id.action_sort:
                showSortDialog();
                return true;
            case R.id.settings:
                Intent settingsIntent = new Intent(getActivity(), SetttingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public abstract Query getQuery(DatabaseReference databaseReference);
}