package ander.kz;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ander.kz.models.Model;
import ander.kz.models.User;

public class NewCardActivity extends AppCompatActivity {
	private DatabaseReference mDatabase;
	private EditText mSongName, mSingerName;
	private FloatingActionButton mSubmitButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_card);
		mSongName = findViewById(R.id.field_SongName);
		mSingerName = findViewById(R.id.field_SingerName);
		mSubmitButton = findViewById(R.id.fab_submit_card);

		mDatabase = FirebaseDatabase.getInstance().getReference();

		mSubmitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				submitCard();
			}
		});
	}

	private boolean validateForm(String SongName, String SingerName) {
		if (TextUtils.isEmpty(SongName)) {
			mSongName.setError(getString(R.string.required));
			return false;
		} else if (TextUtils.isEmpty(SingerName)) {
			mSingerName.setError(getString(R.string.required));
			return false;
		} else {
			mSongName.setError(null);
			mSingerName.setError(null);
			return true;
		}
	}

	private void submitCard() {
		final String songName = mSongName.getText().toString().trim();
		final String singerName = mSingerName.getText().toString().trim();
		final String userId = getUid();

		if (validateForm(songName, singerName)) {
			setEditingEnabled(false);
			mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					User user = dataSnapshot.getValue(User.class);
					if (user == null) {
						Toast.makeText(NewCardActivity.this, "Error: could not fetch user.", Toast.LENGTH_LONG).show();
					} else {
						writeNewCard(userId, user.username, songName, singerName);
					}
					setEditingEnabled(true);
					finish();
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {
					setEditingEnabled(true);
					Toast.makeText(NewCardActivity.this, "onCancelled: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	@SuppressLint("RestrictedApi")
	private void setEditingEnabled(boolean enabled) {
		mSongName.setEnabled(enabled);
		mSingerName.setEnabled(enabled);
		if (enabled) {
			mSubmitButton.setVisibility(View.VISIBLE);
		} else {
			mSubmitButton.setVisibility(View.GONE);
		}
	}

	private void writeNewCard(String userId, String username, String kzSongTitle, String kzSongSinger) {

		String key = mDatabase.child("newSongs").push().getKey();
		Model card = new Model(userId, username, kzSongTitle, kzSongSinger);
		Map<String, Object> cardValues = card.toMap();

		Map<String, Object> childUpdates = new HashMap<>();
		childUpdates.put("/newSongs/" + key, cardValues);
		childUpdates.put("/user-cards/" + userId + "/" + key, cardValues);

		mDatabase.updateChildren(childUpdates);
	}
	public String getUid() {
		return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
	}
}