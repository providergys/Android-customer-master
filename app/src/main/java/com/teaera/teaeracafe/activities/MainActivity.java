package com.teaera.teaeracafe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.teaera.teaeracafe.R;
import com.teaera.teaeracafe.preference.CartPrefs;
import com.teaera.teaeracafe.preference.CategoryPrefs;
import com.teaera.teaeracafe.preference.LocationPrefs;
import com.teaera.teaeracafe.preference.PromotedMenuPrefs;
import com.teaera.teaeracafe.preference.UserPrefs;
import com.teaera.teaeracafe.utils.Constants;


public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private RelativeLayout menuRelativeLayout;
    private TextView menuTextView;
    private ImageView cartImageView;
    private TextView cartTextView;
    private TextView usernameTextView;
    private ImageView photoImageView;

    private boolean menuDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int menuItem = getIntent().getIntExtra("menuItem", 0);

        menuRelativeLayout = findViewById(R.id.menuRelativeLayout);
        menuRelativeLayout.setVisibility(View.GONE);

        menuTextView = findViewById(R.id.menuTextView);
        cartImageView = findViewById(R.id.cartImageView);
        cartTextView = findViewById(R.id.cartTextView);
        usernameTextView = findViewById(R.id.usernameTextView);
        usernameTextView.setText(UserPrefs.getUserInfo(this).getFirstname() + " " + UserPrefs.getUserInfo(this).getLastname());

        photoImageView = findViewById(R.id.photoImageView);
        Picasso.with(this)
                .load(Constants.SERVER_PROFILE_IMAGE_PREFIX + UserPrefs.getUserInfo(this).getImage())
                .into(photoImageView);

        Button dropMenuButton = findViewById(R.id.dropMenuButton);
        dropMenuButton.setOnClickListener(this);

        Button cartButton = findViewById(R.id.cartButton);
        cartButton.setOnClickListener(this);

        Button menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(this);

        Button notificationButton = findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(this);

        Button rewardButton = findViewById(R.id.rewardButton);
        rewardButton.setOnClickListener(this);

        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(this);

        Button helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(this);

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(this);

        selectMenuItem(menuItem);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateCardIcon();
    }

    private void updateCardIcon() {
        if (!CartPrefs.isCartsExists(this) || CartPrefs.getCarts(this).size() == 0) {
            cartImageView.setVisibility(View.INVISIBLE);
            cartTextView.setVisibility(View.INVISIBLE);
        } else {
            int count = CartPrefs.getCarts(this).size();


            cartImageView.setVisibility(View.VISIBLE);
            cartTextView.setVisibility(View.VISIBLE);
            cartTextView.setText(Integer.toString(count));
        }
    }

    public void rewardMenu() {
        //menuTextView.setText(R.string.menu_menu);
        selectMenuItem(0);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.dropMenuButton:
                if (!menuDisplayed)
                    showMenu();
                break;

            case R.id.cartButton:
                Intent intent = new Intent(this, CartActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;

            case R.id.menuButton:
                selectMenuItem(0);
                break;
            case R.id.notificationButton:
                selectMenuItem(1);
                break;
            case R.id.rewardButton:
                selectMenuItem(2);
                break;
            case R.id.profileButton:
                selectMenuItem(3);
                break;
            case R.id.helpButton:
                selectMenuItem(4);
                break;
            case R.id.logoutButton:
                UserPrefs.clearUserInfo(this);
                LocationPrefs.clearLocations(this);
                CategoryPrefs.clearCategories(this);
                PromotedMenuPrefs.clearPromotedMenu(this);
                CartPrefs.clearCarts(this);
                selectMenuItem(5);
                break;
        }
    }

    // Menu
    public void showMenu() {
        Picasso.with(this)
                .load(Constants.SERVER_PROFILE_IMAGE_PREFIX + UserPrefs.getUserInfo(this).getImage())
                .into(photoImageView);
        usernameTextView.setText(UserPrefs.getUserInfo(this).getFirstname() + " " + UserPrefs.getUserInfo(this).getLastname());

        menuDisplayed = true;
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        menuRelativeLayout.startAnimation(animation);
        menuRelativeLayout.setVisibility(View.VISIBLE);
    }

    public void hideMenu() {
        if (!menuDisplayed) {
            return;
        }
        menuDisplayed = false;
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        menuRelativeLayout.startAnimation(animation);
        menuRelativeLayout.setVisibility(View.GONE);
    }


    public void selectMenuItem(int menuItem) {

        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        switch (menuItem) {
            case 0:
                menuTextView.setText(R.string.menu_menu);
                fragmentClass = MenuFragment.class;
                break;
            case 1:
                menuTextView.setText(R.string.menu_notification);
                fragmentClass = NotificationFragment.class;
                break;
            case 2:
                menuTextView.setText(R.string.menu_rewards);
                fragmentClass = RewardsFragment.class;
                break;
            case 3:
                menuTextView.setText(R.string.menu_profile);
                fragmentClass = ProfileFragment.class;
                break;
            case 4:
                menuTextView.setText(R.string.menu_help);
                fragmentClass = HelpFragment.class;
                break;
            case 5:
                Intent intent = new Intent(this, SignInActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                return;
            default:
                fragmentClass = MenuFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit();

        hideMenu();
//        if (!Application.getIsRedeemed()) {
//            hideMenu();
//        }
    }
}
