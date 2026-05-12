package com.barcode.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.barcode.app.MainActivity;
import com.barcode.app.R;
import com.barcode.app.database.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_menu) {
                showPopupMenu(toolbar);
                return true;
            }
            return false;
        });

        view.findViewById(R.id.btn_scan_query).setOnClickListener(v ->
                ((MainActivity) requireActivity()).loadFragment(new ScanQueryFragment()));

        view.findViewById(R.id.btn_scan_entry).setOnClickListener(v ->
                ((MainActivity) requireActivity()).loadFragment(new ScanEntryFragment()));
    }

    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.home_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_view_list) {
                ((MainActivity) requireActivity()).loadFragment(new ProductListFragment());
                return true;
            } else if (id == R.id.menu_import) {
                doImport();
                return true;
            } else if (id == R.id.menu_export) {
                doExport();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void doImport() {
        File importFile = new File(requireContext().getExternalFilesDir(null), "products_import.csv");
        if (!importFile.exists()) {
            Toast.makeText(requireContext(), "请将 products_import.csv 放入应用外部存储目录", Toast.LENGTH_LONG).show();
            return;
        }
        DatabaseHelper db = ((MainActivity) requireActivity()).getDbHelper();
        int count = db.importFromCsv(importFile);
        Toast.makeText(requireContext(), "成功导入 " + count + " 条商品数据", Toast.LENGTH_SHORT).show();
    }

    private void doExport() {
        File exportFile = new File(requireContext().getExternalFilesDir(null), "products_export.csv");
        DatabaseHelper db = ((MainActivity) requireActivity()).getDbHelper();
        int count = db.exportToCsv(exportFile);
        Toast.makeText(requireContext(), "已导出 " + count + " 条数据到: " + exportFile.getAbsolutePath(),
                Toast.LENGTH_LONG).show();
    }
}
