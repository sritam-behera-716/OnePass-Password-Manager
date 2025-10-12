package com.securevault.onepass.ui.main.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.securevault.onepass.R;
import com.securevault.onepass.data.DatabaseHelper;
import com.securevault.onepass.data.PasswordItem;
import com.securevault.onepass.databinding.FragmentHomeBinding;
import com.securevault.onepass.utils.SearchViewHelper;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private ArrayList<PasswordItem> allPasswordItems;
    private RecyclerViewAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchDataFromDatabase();
        setUpSearchView();
        showPasswordItems();
    }

    private final ActivityResultLauncher<Intent> detailsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    fetchDataFromDatabase();
                    showPasswordItems();
                    savePasswordStoreNumber();
                }
            });

    private void fetchDataFromDatabase() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(requireContext());
        allPasswordItems = (ArrayList<PasswordItem>) databaseHelper.passwordDao().retrieveRecord();
    }

    private void setUpSearchView() {
        binding.searchView.clearFocus();
        SearchViewHelper.setStrokeColorByTextListener(requireContext(), binding.searchView);
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText.trim());
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
    }

    public void filterList(String text) {
        if (text.isEmpty()) {
            showPasswordItems();
            return;
        }

        ArrayList<PasswordItem> filteredList = new ArrayList<>();
        for (PasswordItem item : allPasswordItems) {
            if (item.getPasswordName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty()) {
            showNoResult(false);
        } else {
            adapter.setFilteredList(filteredList);
        }
    }

    public void showPasswordItems() {
        if (allPasswordItems.isEmpty()) {
            showNoResult(true);
            return;
        }

        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.searchIllustration.setVisibility(View.GONE);
        binding.title.setVisibility(View.GONE);
        binding.description.setVisibility(View.GONE);

        savePasswordStoreNumber();

        adapter = new RecyclerViewAdapter(requireContext(), allPasswordItems, item -> {
            Intent intent = new Intent(requireContext(), DetailsActivity.class);
            intent.putExtra("id", item.getId());
            intent.putExtra("password_name", item.getPasswordName());
            intent.putExtra("created_date", item.getCreatedDate().toString());
            intent.putExtra("url", item.getUrl());
            intent.putExtra("username", item.getUsername());
            intent.putExtra("password_length", item.getPasswordLength());
            detailsLauncher.launch(intent);
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void savePasswordStoreNumber() {
        binding.passwordStoreNumber.setText(String.valueOf(allPasswordItems.size()));
    }

    private void showNoResult(boolean flag) {
        if (flag) {
            binding.title.setText(R.string.no_passwords);
            binding.description.setText(R.string.no_passwords_stored);
        }

        binding.recyclerView.setVisibility(View.GONE);
        binding.searchIllustration.setVisibility(View.VISIBLE);
        binding.title.setVisibility(View.VISIBLE);
        binding.description.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}