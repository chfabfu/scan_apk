package com.barcode.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.barcode.app.MainActivity;
import com.barcode.app.R;
import com.barcode.app.database.DatabaseHelper;
import com.barcode.app.model.Product;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class ProductInfoFragment extends Fragment {

    private TextInputEditText etBarcode, etName, etPrice;
    private MaterialButton btnEdit, btnDelete, btnConfirm, btnCancel;
    private View editButtonsLayout, actionButtonsLayout;
    private MaterialToolbar toolbar;

    private String mode;
    private boolean editing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        etBarcode = view.findViewById(R.id.et_barcode);
        etName = view.findViewById(R.id.et_name);
        etPrice = view.findViewById(R.id.et_price);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnDelete = view.findViewById(R.id.btn_delete);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        btnCancel = view.findViewById(R.id.btn_cancel);
        editButtonsLayout = view.findViewById(R.id.edit_buttons_layout);
        actionButtonsLayout = view.findViewById(R.id.action_buttons_layout);

        Bundle args = getArguments();
        if (args != null) {
            mode = args.getString("mode", "query");
            etBarcode.setText(args.getString("barcode", ""));
            etName.setText(args.getString("name", ""));
            double price = args.getDouble("price", 0);
            if (price != 0) {
                etPrice.setText(String.valueOf(price));
            }
            applyMode();
        }

        btnEdit.setOnClickListener(v -> toggleEdit());
        btnDelete.setOnClickListener(v -> confirmDelete());
        btnConfirm.setOnClickListener(v -> onConfirm());
        btnCancel.setOnClickListener(v -> onCancel());
    }

    private void applyMode() {
        boolean isQuery = "query".equals(mode);
        toolbar.setTitle(isQuery ? "商品信息 - 查询" : "商品信息 - 录入");
        setReadOnly(isQuery);
        etBarcode.setEnabled(false);
        editButtonsLayout.setVisibility(isQuery ? View.VISIBLE : View.GONE);
        actionButtonsLayout.setVisibility(isQuery ? View.GONE : View.VISIBLE);
    }

    private void setReadOnly(boolean readOnly) {
        etName.setEnabled(!readOnly);
        etPrice.setEnabled(!readOnly);
        if (readOnly) {
            clearFocusAndHideKeyboard();
        }
    }

    private void clearFocusAndHideKeyboard() {
        View focused = requireActivity().getCurrentFocus();
        if (focused != null) {
            focused.clearFocus();
            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
            }
        }
    }

    private void setQueryMode() {
        editing = false;
        mode = "query";
        btnEdit.setText("编辑");
        applyMode();
    }

    private void toggleEdit() {
        editing = !editing;
        if (editing) {
            setReadOnly(false);
            btnEdit.setText("取消编辑");
            actionButtonsLayout.setVisibility(View.VISIBLE);
        } else {
            restoreOriginal();
            setQueryMode();
        }
    }

    private void restoreOriginal() {
        Bundle args = getArguments();
        if (args != null) {
            etName.setText(args.getString("name", ""));
            double price = args.getDouble("price", 0);
            etPrice.setText(price != 0 ? String.valueOf(price) : "");
        }
    }

    private String textOf(TextInputEditText et) {
        return et.getText().toString().trim();
    }

    private void onConfirm() {
        String name = textOf(etName);
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "请输入商品名称", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(textOf(etPrice));
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "请输入有效的价格", Toast.LENGTH_SHORT).show();
            return;
        }

        String barcode = textOf(etBarcode);
        DatabaseHelper db = ((MainActivity) requireActivity()).getDbHelper();
        db.saveProduct(new Product(barcode, name, price));

        Toast.makeText(requireContext(),
                "entry".equals(mode) ? "录入成功" : "修改成功", Toast.LENGTH_SHORT).show();

        Bundle args = getArguments();
        args.putString("name", name);
        args.putDouble("price", price);

        setQueryMode();
    }

    private void onCancel() {
        if ("query".equals(mode)) {
            restoreOriginal();
            setQueryMode();
        } else {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage("确定要删除该商品吗？")
                .setPositiveButton("是", (d, w) -> doDelete())
                .setNegativeButton("否", null)
                .show();
    }

    private void doDelete() {
        ((MainActivity) requireActivity()).getDbHelper().deleteProduct(textOf(etBarcode));
        Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}
