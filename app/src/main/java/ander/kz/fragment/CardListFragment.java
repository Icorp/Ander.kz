package ander.kz.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;
import com.shreyaspatil.firebase.recyclerpagination.LoadingState;

import java.util.Objects;

import ander.kz.CardDetailActivity;
import ander.kz.R;
import ander.kz.Search;
import ander.kz.SetttingsActivity;
import ander.kz.models.Model;
import ander.kz.viewholder.CardViewHolder;

public abstract class CardListFragment extends Fragment {
    private DatabaseReference mDatabase;
    private FirebaseRecyclerPagingAdapter<Model, CardViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Query mQuery;


    //For Sorting
    private LinearLayoutManager mLayoutManager;

    //For saving sort setting
    private SharedPreferences mSharedPref;


    public CardListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        View rootView;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //PaginationAdapter
        if (getStringQuery("").equals("songsDB")) {
            rootView = inflater.inflate(R.layout.firebase_pagination, container, false);
            mRecycler = rootView.findViewById(R.id.recycler_view);
            mQuery = mDatabase.child("songsDB");

            mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

            setUpAdapterPagination();
        }

        //NoPaginationAdapter
        else {
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


            rootView = inflater.inflate(R.layout.fragments_all_cards, container, false);
            mRecycler = rootView.findViewById(R.id.messages_list);
            setUpAdapterNoPagination();
        }

        mRecycler.setHasFixedSize(true);
        mDatabase.keepSynced(true);
        return rootView;
    }

    private void setUpAdapterPagination() {


        //Initialize Paging Configurations
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .setPageSize(30)
                .build();

        //Initialize Firebase Paging Options
        DatabasePagingOptions<Model> options = new DatabasePagingOptions.Builder<Model>()
                .setLifecycleOwner(this)
                .setQuery(mQuery, config, Model.class)
                .build();

        //Initializing Adapter
        mAdapter = new FirebaseRecyclerPagingAdapter<Model, CardViewHolder>(options) {
            @NonNull
            @Override
            public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new CardViewHolder(inflater.inflate(R.layout.item_card, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull CardViewHolder viewHolder, int i, @NonNull Model model) {
                final DatabaseReference cardRef = getRef(i);

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

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                    case LOADING_MORE:
                        mSwipeRefreshLayout.setRefreshing(true);
                        break;
                    case LOADED:
                    case FINISHED:
                        mSwipeRefreshLayout.setRefreshing(false);
                        break;
                    case ERROR:
                        break;
                }
            }

            @Override
            protected void onError(DatabaseError databaseError) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        };

        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter.startListening();
        mRecycler.setAdapter(mAdapter);

        // Reload data on swipe
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Reload Data
                mAdapter.refresh();
            }
        });
    }

    private void setUpAdapterNoPagination() {
        //Sorting
        mSharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences("SortSetting", Context.MODE_PRIVATE);
        String mSorting = mSharedPref.getString("Sort", "newest");


        if (mSorting.equals("newest")) {
            //This will load the items from button means newest first
            mLayoutManager = new LinearLayoutManager(getActivity());
            mLayoutManager.setReverseLayout(true);
            mLayoutManager.setStackFromEnd(true);
        } else if (mSorting.equals("oldest")) {
            //This will load the items from bottom means oldest first
            mLayoutManager = new LinearLayoutManager(getActivity());
            mLayoutManager.setReverseLayout(false);
            mLayoutManager.setStackFromEnd(false);
        }
        mRecycler.setLayoutManager(mLayoutManager);

        Query cardsQuery = getQuery(mDatabase);

        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(cardsQuery, Model.class)
                .build();

        final FirebaseRecyclerAdapter mAdapter2 = new FirebaseRecyclerAdapter<Model, CardViewHolder>(options) {
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
        };
        mAdapter2.startListening();
        mRecycler.setAdapter(mAdapter2);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void showSortDialog() {
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
                            editor.putString("Sort", "newest"); // where 'Sort' is key & 'newest' is value
                            editor.apply();//apply / save the value in our shared preferences
                            getActivity().recreate(); //Restart acitivity to take effect
                        } else if (which == 1) {
                            //Sort by oldest
                            //Edit our shared prefences
                            SharedPreferences.Editor editor = mSharedPref.edit();
                            editor.putString("Sort", "oldest"); // where 'Sort' is key & 'newest' is value
                            editor.apply();//apply / save the value in our shared preferences
                            getActivity().recreate(); //Restart acitivity to take effect
                        }
                    }
                });
        builder.show();
    }

    //Liked
    private void onStarClicked(DatabaseReference cardRef) {
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
    String getUid() {
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

    public abstract String getStringQuery(String databaseString);
}