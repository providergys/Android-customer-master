package com.teaera.teaeracafe.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.teaera.teaeracafe.R;
import com.teaera.teaeracafe.net.Model.OrderDetailsInfo;

import java.util.ArrayList;

/**
 * Created by admin on 15/12/2017.
 */

public class RefundOrderAdapter  extends BaseAdapter {

    private static LayoutInflater inflater=null;
    private Context context;
    private ArrayList<OrderDetailsInfo> orderDetails;
    private OnQuantityChangeListener onQuantityChangeListener;

    public RefundOrderAdapter(Activity activity, ArrayList<OrderDetailsInfo> orderDetails) {

        this.context = activity;
        this.orderDetails = orderDetails;
        this.onQuantityChangeListener = (OnQuantityChangeListener) activity;

        inflater = (LayoutInflater)context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateCategories(ArrayList<OrderDetailsInfo> orderDetails) {
        this.orderDetails = orderDetails;
    }

    @Override
    public int getCount() {
        return orderDetails.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public class Holder
    {
        RelativeLayout quantityLayout;
        TextView menuTextView;
        TextView costTextView;
        TextView detailsTextView;
        TextView quantityTextView;
        Button plusButton;
        Button minusButton;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {

        final RefundOrderAdapter.Holder holder = new RefundOrderAdapter.Holder();
        View rowView = inflater.inflate(R.layout.refund_order, null);

        holder.quantityLayout = rowView.findViewById(R.id.relativeLayout11);
        holder.menuTextView = rowView.findViewById(R.id.menuTextView);
        holder.detailsTextView = rowView.findViewById(R.id.detailsTextView);
        holder.costTextView = rowView.findViewById(R.id.costTextView);
        holder.quantityTextView = rowView.findViewById(R.id.quantityTextView);
        holder.plusButton = rowView.findViewById(R.id.quantityPlusButton);
        holder.minusButton = rowView.findViewById(R.id.quantityMinusButton);

        holder.menuTextView.setText(orderDetails.get(position).getMenuName());
        holder.detailsTextView.setText(orderDetails.get(position).getOptions());
        holder.quantityTextView.setText(orderDetails.get(position).getQuantity());
        holder.costTextView.setText(String.format("$%.2f", Float.parseFloat(orderDetails.get(position).getPrice())));

        if (orderDetails.get(position).getRefunded().equals("1")) {
            holder.quantityLayout.setVisibility(View.GONE);
        } else {
            holder.quantityLayout.setVisibility(View.VISIBLE);
        }

        holder.plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onQuantityChangeListener.onPlusButtonClicked(position);
            }
        });

        holder.minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num = Integer.parseInt(orderDetails.get(position).getQuantity());
                if (num == 0)
                    return;

                onQuantityChangeListener.onMinusButtonClicked(position);
            }
        });

        return rowView;
    }

    public interface OnQuantityChangeListener {
        public void onPlusButtonClicked(int position);
        public void onMinusButtonClicked(int position);
    }
}

