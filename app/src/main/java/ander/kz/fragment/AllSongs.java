package ander.kz.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class AllSongs extends CardListFragment {
	public AllSongs() {
	}

	@Override
	public Query getQuery(DatabaseReference databaseReference) {
		return databaseReference.child("songsDB");
	}
}