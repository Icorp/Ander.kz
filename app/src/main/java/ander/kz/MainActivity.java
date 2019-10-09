package ander.kz;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import ander.kz.fragment.AllSongs;
import ander.kz.fragment.MySongs;
import ander.kz.fragment.TopSongs;

///////////////////////////////////////////
/*
To Do
1)Оптимизировать код(убрать дублирование кода)
2)Проверить все файлы и импорты
3)Проверить неиспользуемые вещи
4)Придумать нормальные имена для переменных , а так же для самих файлов
*/
//////////////////////////////////////////

public class MainActivity extends AppCompatActivity {

	private FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mAuth = FirebaseAuth.getInstance();

		FragmentPagerAdapter mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			private final Fragment[] mFragments = new Fragment[] {
					new AllSongs(),
					new TopSongs(),
					new MySongs(),
			};

			@Override
			public Fragment getItem(int position) {
				return mFragments[position];
			}
			@Override
			public int getCount() {
				return mFragments.length;
			}
			@Override
			public CharSequence getPageTitle(int position) {
				return getResources().getStringArray(R.array.headings)[position];
			}
		};

		ViewPager mViewPager = findViewById(R.id.container);
		mViewPager.setAdapter(mPagerAdapter);

		TabLayout tabLayout = findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);

		findViewById(R.id.fab_new_card).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, NewCardActivity.class));
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		FirebaseUser currentUser = mAuth.getCurrentUser();
		updateUI(currentUser);
	}

	private void updateUI(FirebaseUser user) {
		if(user ==null){
			mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					if(task.isSuccessful()){
						FirebaseUser user = mAuth.getCurrentUser();
						updateUI(user);
					}else{
						updateUI(null);
					}
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

}