package ander.kz.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MySongs extends CardListFragment {
    public MySongs() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // My top posts by number of stars
        return databaseReference.child("songsDB")
                .orderByChild("stars/"+getUid())
                .equalTo(true);

    }
}