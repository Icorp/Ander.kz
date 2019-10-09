package ander.kz;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;


import ander.kz.models.Model;


public class CardDetailActivity extends AppCompatActivity {
	private static final String TAG = "CardDetailActivity";
	public static final String EXTRA_CARD_KEY = "card_key";
	private ValueEventListener mCardListener;

	public DatabaseReference mCardReference;
	public TextView mSongName, mSingerName, mComposer, mAuthor, mSongText,video_id;
	public AdView mAdView;
	public String Video_Id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card_detail);

		//Admob
		MobileAds.initialize(this, "YOUR_ADMOB_APP_ID");
		AdView adView = new AdView(this);
		adView.setAdSize(AdSize.BANNER);
		adView.setAdUnitId("ca-app-pub-1592364494487356~7897871122");
		mAdView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

		//YoutubePlayerAPi
        YouTubePlayerView youTubePlayerView = findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
			@Override
			public void onReady(@NonNull YouTubePlayer youTubePlayer) {
				System.out.println(Video_Id);
				if (Video_Id.length() > 0)
					youTubePlayer.loadVideo(Video_Id.substring(32),0);
				else {
					youTubePlayer.cueVideo(Video_Id,0);
				}
			}
		});

		//Initiliazing
		mSongName = findViewById(R.id.textViewSongNameGet);
		mSingerName = findViewById(R.id.textViewSingerNameGet);
		mComposer = findViewById(R.id.textViewComposerGet);
		mAuthor = findViewById(R.id.textViewAuthorGet);
		mSongText = findViewById(R.id.textViewSongText);
		video_id = findViewById(R.id.video_id);

		// Get card key from intent
		String mCardKey = getIntent().getStringExtra(EXTRA_CARD_KEY);
		if (mCardKey == null) {
			throw new IllegalArgumentException("Must pass EXTRA_CARD_KEY");
		}
		// Initialize Database
		mCardReference = FirebaseDatabase.getInstance().getReference().child("songsDB").child(mCardKey);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Add value event listener to the card
		ValueEventListener cardListener = new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				// Get Card object and use the values to update the UI
				Model card = dataSnapshot.getValue(Model.class);

				//Set data to views
				mSongName.setText(card.kzSongTitle);
				mSingerName.setText(card.kzSongSinger);
				mComposer.setText(card.kzSongComposerName);
				mAuthor.setText(card.kzSongAuthorName);
				mSongText.setText(card.kzSongText);
				video_id.setText(card.kzSongytblink);
				Video_Id = video_id.getText().toString();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				// Getting Card failed, log a message
				Log.w(TAG, "loadCard:onCancelled", databaseError.toException());
				Toast.makeText(CardDetailActivity.this, "Failed to load card.", Toast.LENGTH_SHORT).show();
			}
		};
		mCardReference.addValueEventListener(cardListener);

		// Keep copy of card listener so we can remove it when app stops
		mCardListener = cardListener;

	}

	@Override
	public void onStop() {
		super.onStop();
		if (mCardListener != null) {
			mCardReference.removeEventListener(mCardListener);
		}
	}
}