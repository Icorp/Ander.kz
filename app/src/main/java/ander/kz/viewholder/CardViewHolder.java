package ander.kz.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import ander.kz.R;
import ander.kz.models.Model;

public class CardViewHolder extends RecyclerView.ViewHolder {
	public TextView numStarsView;
	public ImageView mImage;
	public TextView mSongName;
	public TextView mSingerName;
	public ImageButton starView;

	public CardViewHolder(View itemView) {
		super(itemView);
		mSongName = itemView.findViewById(R.id.SongName);
		mSingerName = itemView.findViewById(R.id.SongSinger);
		mImage = itemView.findViewById(R.id.checkYoutubeLink);
		numStarsView = itemView.findViewById(R.id.post_num_stars);
		starView = itemView.findViewById(R.id.star);
	}

	public void bindToCard(Model model, View.OnClickListener onClickListener) {
		mSongName.setText(model.kzSongTitle);
		mSingerName.setText(model.kzSongSinger);
		numStarsView.setText(String.valueOf(model.starCount));
		starView.setOnClickListener(onClickListener);
	}
}