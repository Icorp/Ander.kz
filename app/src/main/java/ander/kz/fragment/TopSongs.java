package ander.kz.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class TopSongs extends CardListFragment {
    public TopSongs() {}
    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child("songsDB").limitToLast(30).orderByChild("starCount");
    }

    @Override
    public String getStringQuery(String databaseString) {
        return "anything";
    }
}