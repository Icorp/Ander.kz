package ander.kz;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import ander.kz.fragment.CardListFragment;
import ander.kz.models.Model;
import ander.kz.viewholder.CardViewHolder;

import static androidx.appcompat.widget.SearchView.*;

public class Search extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private RecyclerView mRecycler;

    FirebaseDatabase mFirebaseDatabase;
    Query mRef;
    FirebaseRecyclerOptions options;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        findViewById(R.id.fab_new_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Search.this, NewCardActivity.class));
            }
        });

        mRecycler = findViewById(R.id.search_recycler);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference("songsDB");

        mDatabase.keepSynced(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu; this adds items to the action bar if it present
        getMenuInflater().inflate(R.menu.second_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchData(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Filter as you type
                searchData(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    public String firstUpperCase(String word){
        if(word == null || word.isEmpty()) return "";
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
    public  void searchData(String searchText){

        LinearLayoutManager mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        Query firebaseSearchQuery = mRef.orderByChild("kzSongTitle").startAt(firstUpperCase(searchText)).endAt(firstUpperCase(searchText)+"\uf8ff");

        options = new FirebaseRecyclerOptions.Builder<Model>().setQuery(firebaseSearchQuery, Model.class).build();

        // Need to write to both places the post is stored
        // Run two transactions
        FirebaseRecyclerAdapter mAdapter = new FirebaseRecyclerAdapter<Model, CardViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CardViewHolder viewHolder, int position, @NonNull final Model model) {
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
            }
        };

        mRecycler.setLayoutManager(mManager);
        mAdapter.startListening();

        //set adapter to firebase recycler
        mRecycler.setAdapter(mAdapter);
    }


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
    public String getUid() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }
}
