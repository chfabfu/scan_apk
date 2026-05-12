package com.barcode.app.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.barcode.app.MainActivity;
import com.barcode.app.R;
import com.barcode.app.model.Product;
import com.google.android.material.appbar.MaterialToolbar;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ScanEntryFragment extends Fragment {

    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                String barcode = result.getContents();
                if (barcode != null) {
                    onBarcodeResult(barcode);
                }
            });

    private final ActivityResultLauncher<String> cameraPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startScan();
                } else {
                    Toast.makeText(requireContext(), "需要相机权限才能扫描", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_entry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        view.findViewById(R.id.btn_scan).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                startScan();
            } else {
                cameraPermLauncher.launch(Manifest.permission.CAMERA);
            }
        });
    }

    private void startScan() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES);
        options.setPrompt("扫描条形码");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(false);
        scanLauncher.launch(options);
    }

    private MainActivity getMain() {
        return (MainActivity) requireActivity();
    }

    private void onBarcodeResult(String barcode) {
        Product product = getMain().getDbHelper().getProduct(barcode);
        Bundle args = new Bundle(4);
        args.putString("barcode", barcode);
        args.putString("mode", "entry");
        if (product != null) {
            args.putString("name", product.getName());
            args.putDouble("price", product.getPrice());
        } else {
            args.putString("name", "");
            args.putDouble("price", 0);
        }
        ProductInfoFragment fragment = new ProductInfoFragment();
        fragment.setArguments(args);
        getMain().loadFragment(fragment);
    }
}
