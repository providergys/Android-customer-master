package com.teaera.teaeracafe.activities;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.teaera.teaeracafe.R;
import com.teaera.teaeracafe.adapter.MenuListAdapter;
import com.teaera.teaeracafe.app.Application;
import com.teaera.teaeracafe.net.Model.CategoryInfo;
import com.teaera.teaeracafe.net.Model.LocationInfo;
import com.teaera.teaeracafe.net.Model.PromotedMenuInfo;
import com.teaera.teaeracafe.net.Request.GetPromotedCategoryRequest;
import com.teaera.teaeracafe.net.Request.GetPromotedMenuRequest;
import com.teaera.teaeracafe.net.Response.PromotedCategoryResponse;
import com.teaera.teaeracafe.net.Response.PromotedMenuResponse;
import com.teaera.teaeracafe.preference.CategoryPrefs;
import com.teaera.teaeracafe.preference.LocationPrefs;
import com.teaera.teaeracafe.preference.PromotedMenuPrefs;
import com.teaera.teaeracafe.utils.DialogUtils;
import com.teaera.teaeracafe.utils.ViewPagerIndicator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MenuFragment extends Fragment implements MenuItemFragment.OnPromotedMenuClickListener {

    private Spinner locationSpinner;
    private TextView locationTextView;
    private ListView menuListView;

    private PromotionAdapter promotionAdapter;
    private List<MenuItemFragment> fragments = new ArrayList<>();
    private RelativeLayout bannerRelativeLayout;
    private ViewPager viewPager;
    private ViewPagerIndicator indicator;

    private PromotionAdapter promotionAdapter1;
    private List<MenuItemFragment> fragments1 = new ArrayList<>();
    private RelativeLayout bannerRelativeLayout1;
    private ViewPager viewPager1;
    private ViewPagerIndicator indicator1;

    private MenuListAdapter menuListAdapter;
    private ArrayList<LocationInfo> locations;
    private ArrayList<CategoryInfo> categories;
    private ArrayList<String> locationNames = new ArrayList<String>();
    private ArrayList<CategoryInfo> sortedCategories = new ArrayList<CategoryInfo>();
    private ArrayList<PromotedMenuInfo> promotedMenuInfos = new ArrayList<PromotedMenuInfo>();
    private ArrayList<PromotedMenuInfo> sortedPromotedMenus = new ArrayList<PromotedMenuInfo>();


    private MenuItemFragment.OnPromotedMenuClickListener onPromotedMenuClickListener;
    private ProgressDialog dialog;

    public MenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        init();
    }

    @Override
    public void onResume() {
        super.onResume();

        int location = Application.getLocation();

        locationTextView = getActivity().findViewById(R.id.locationTextView1);
        locationTextView.setText(locationNames.get(location));

        menuListView = getActivity().findViewById(R.id.menuListView);
        menuListAdapter = new MenuListAdapter(this.getActivity(), sortedCategories);
        menuListView.setAdapter(menuListAdapter);
        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), SubMenuActivity.class);
                intent.putExtra("category", sortedCategories.get(position));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });

        locationSpinner.setSelection(location);
    }

    public void init() {

        bannerRelativeLayout = getActivity().findViewById(R.id.bannerRelativeLayout);
        viewPager = getActivity().findViewById(R.id.viewpager);
        indicator = getActivity().findViewById(R.id.pageIndicator);

        bannerRelativeLayout1 = getActivity().findViewById(R.id.bannerRelativeLayout1);
        viewPager1 = getActivity().findViewById(R.id.viewpager1);
        indicator1 = getActivity().findViewById(R.id.pageIndicator1);

        locations = LocationPrefs.getLocations(this.getActivity());
        categories = CategoryPrefs.getCategories(this.getActivity());
        promotedMenuInfos = PromotedMenuPrefs.getPromotedMenu(this.getActivity());

        for (int i = 0; i < locations.size(); i++) {
            locationNames.add(locations.get(i).getLocationName());
        }

        loadPromotion();

        locationSpinner = getActivity().findViewById(R.id.locationSpinner);
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                locationTextView.setText(locationSpinner.getSelectedItem().toString());
                Application.setLocation(position);
                updatePromotion(position);
                updateCategories(position);
                menuListAdapter.updateCategories(sortedCategories);
                menuListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_item,locationNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);
        locationSpinner.setSelection(0);

        bannerRelativeLayout.setVisibility(View.VISIBLE);
        bannerRelativeLayout.setVisibility(View.GONE);
    }


    private void loadPromotion() {
        sortedPromotedMenus.clear();
        for (int i=0; i<promotedMenuInfos.size(); i++) {
            if (promotedMenuInfos.get(i).getLocationId().equals(locations.get(0).getId())) {
                sortedPromotedMenus.add(promotedMenuInfos.get(i));
            }
        }

        for (int i = 0; i < sortedPromotedMenus.size(); i++) {
            fragments.add(MenuItemFragment.createInstance(sortedPromotedMenus.get(i), (MenuItemFragment.OnPromotedMenuClickListener) this));
        }

        promotionAdapter = new PromotionAdapter(getChildFragmentManager(), fragments);
        viewPager.setAdapter(promotionAdapter);
        indicator.setPager(viewPager);


        sortedPromotedMenus.clear();
        for (int i=0; i<promotedMenuInfos.size(); i++) {
            if (promotedMenuInfos.get(i).getLocationId().equals(locations.get(1).getId())) {
                sortedPromotedMenus.add(promotedMenuInfos.get(i));
            }
        }

        for (int i = 0; i < sortedPromotedMenus.size(); i++) {
            fragments1.add(MenuItemFragment.createInstance(sortedPromotedMenus.get(i), (MenuItemFragment.OnPromotedMenuClickListener) this));
        }
        promotionAdapter1 = new PromotionAdapter(getChildFragmentManager(), fragments1);
        viewPager1.setAdapter(promotionAdapter1);
        indicator1.setPager(viewPager1);
    }

    private void updatePromotion(int index) {
        if (index == 0) {
            bannerRelativeLayout.setVisibility(View.VISIBLE);
            bannerRelativeLayout1.setVisibility(View.GONE);
        } else {
            bannerRelativeLayout.setVisibility(View.GONE);
            bannerRelativeLayout1.setVisibility(View.VISIBLE);
        }
    }

    private void updateCategories(int index) {

        sortedCategories.clear();
        for (int i=0; i<categories.size(); i++) {
            if (categories.get(i).getLocationID().equals(locations.get(index).getId())) {
                sortedCategories.add(categories.get(i));
            }
        }
    }

    @Override
    public void onPromotedMenuClicked(String menuId, String categoryId) {

        showLoader(R.string.empty);

        Application.getServerApi().getMenu(new GetPromotedMenuRequest(menuId, categoryId)).enqueue(new Callback<PromotedMenuResponse>(){

            @Override
            public void onResponse(Call<PromotedMenuResponse> call, Response<PromotedMenuResponse> response) {
                hideLoader();
                if (response.body().isError()) {
                    DialogUtils.showDialog(getActivity(), "Error", response.body().getMessage(), null, null);
                } else {
                    Intent intent = new Intent(getActivity(), ItemCustomizeActivity.class);
                    intent.putExtra("menu", response.body().getMenu());
                    intent.putExtra("category", response.body().getCategory());
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
            }

            @Override
            public void onFailure(Call<PromotedMenuResponse> call, Throwable t) {
                hideLoader();
                if (t.getLocalizedMessage() != null) {
                    Log.d("PromotedMenu", t.getLocalizedMessage());
                } else {
                    Log.d("PromotedMenu", "Unknown error");
                }
            }
        });
    }

    @Override
    public void onPromotedCategoryClicked(String categoryId) {

        showLoader(R.string.empty);

        Application.getServerApi().getCategory(new GetPromotedCategoryRequest(categoryId)).enqueue(new Callback<PromotedCategoryResponse>(){

            @Override
            public void onResponse(Call<PromotedCategoryResponse> call, Response<PromotedCategoryResponse> response) {
                hideLoader();
                if (response.body().isError()) {
                    DialogUtils.showDialog(getActivity(), "Error", response.body().getMessage(), null, null);
                } else {
                    Intent intent = new Intent(getActivity(), SubMenuActivity.class);
                    intent.putExtra("category", response.body().getCategory());
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
            }

            @Override
            public void onFailure(Call<PromotedCategoryResponse> call, Throwable t) {
                hideLoader();
                if (t.getLocalizedMessage() != null) {
                    Log.d("PromotedCategory", t.getLocalizedMessage());
                } else {
                    Log.d("PromotedCategory", "Unknown error");
                }
            }
        });


    }


    public void showLoader(int resId) {
        dialog = ProgressDialog.show(getActivity(), "",
                getString(resId), true);
    }

    public void hideLoader() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }


    public static class PromotionAdapter extends FragmentStatePagerAdapter {

        private List<MenuItemFragment> fragments;

        public PromotionAdapter(FragmentManager fm, List<MenuItemFragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void updateMenu(List<MenuItemFragment> fragments) {
            this.fragments = fragments;
        }
    }
}
