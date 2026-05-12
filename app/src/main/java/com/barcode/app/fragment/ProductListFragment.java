package com.barcode.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barcode.app.MainActivity;
import com.barcode.app.R;
import com.barcode.app.database.DatabaseHelper;
import com.barcode.app.model.Product;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class ProductListFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        recyclerView = view.findViewById(R.id.recycler_view);
        tvEmpty = view.findViewById(R.id.tv_empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        DatabaseHelper db = ((MainActivity) requireActivity()).getDbHelper();
        List<Product> products = db.getAllProducts();

        if (products.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        if (adapter == null) {
            adapter = new ProductAdapter();
            recyclerView.setAdapter(adapter);
        }
        adapter.updateData(products);
    }

    private class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {
        private List<Product> products = new ArrayList<>();

        void updateData(List<Product> newList) {
            DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override public int getOldListSize() { return products.size(); }
                @Override public int getNewListSize() { return newList.size(); }
                @Override public boolean areItemsTheSame(int o, int n) {
                    return products.get(o).getBarcode().equals(newList.get(n).getBarcode());
                }
                @Override public boolean areContentsTheSame(int o, int n) {
                    Product op = products.get(o), np = newList.get(n);
                    return op.getName().equals(np.getName()) && op.getPrice() == np.getPrice();
                }
            });
            products = newList;
            diff.dispatchUpdatesTo(this);
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Product p = products.get(position);
            holder.tvName.setText(p.getName());
            holder.tvBarcode.setText(p.getBarcode());
            holder.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("barcode", p.getBarcode());
                args.putString("name", p.getName());
                args.putDouble("price", p.getPrice());
                args.putString("mode", "query");
                ProductInfoFragment fragment = new ProductInfoFragment();
                fragment.setArguments(args);
                ((MainActivity) requireActivity()).loadFragment(fragment);
            });
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvBarcode;
            VH(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_product_name);
                tvBarcode = itemView.findViewById(R.id.tv_product_barcode);
            }
        }
    }
}
