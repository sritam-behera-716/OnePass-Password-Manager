package com.securevault.onepass.ui.main.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.securevault.onepass.R;
import com.securevault.onepass.data.PasswordItem;
import com.securevault.onepass.utils.BiometricHelper;
import com.securevault.onepass.utils.ClipboardHelper;
import com.securevault.onepass.utils.SecureEncryptionHelper;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<PasswordItem> passwordItemList;
    private final RecyclerViewAdapter.OnItemClickListener listener;
    private int lastPosition = -1;

    public RecyclerViewAdapter(Context context, ArrayList<PasswordItem> passwordItemList, RecyclerViewAdapter.OnItemClickListener listener) {
        this.context = context;
        this.passwordItemList = passwordItemList;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFilteredList(ArrayList<PasswordItem> filteredPasswordItemList) {
        this.passwordItemList.clear();
        this.passwordItemList.addAll(filteredPasswordItemList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PasswordItem passwordItem = passwordItemList.get(position);

        holder.passwordFont.setText(String.valueOf(passwordItem.getPasswordName().charAt(0)));
        holder.passwordName.setText(passwordItem.getPasswordName());

        holder.copyIcon.setOnClickListener(v -> {
            final BiometricHelper biometricHelper = new BiometricHelper(context);
            biometricHelper.setBiometricCallback(new BiometricHelper.BiometricCallback() {
                @Override
                public void onSuccess() {
                    copyPassword(holder, passwordItem.getEncryptedPassword());
                }

                @Override
                public void onFailed() {

                }
            });
            biometricHelper.checkAndShowBiometricPrompt();
        });

        holder.rootView.setOnClickListener(v -> listener.onItemClick(passwordItem));
        showAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return passwordItemList.size();
    }

    private void showAnimation(View itemView, int position) {
        if (position > lastPosition) {
            Animation slideInLeft = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            itemView.setAnimation(slideInLeft);
            lastPosition = position;
        }
    }

    private void copyPassword(ViewHolder holder, String password) {
        holder.copyIcon.setEnabled(false);

        holder.rootView.setBackgroundResource(R.drawable.item_background_copied);
        holder.passwordFont.setBackgroundResource(R.drawable.password_background_copied);
        holder.passwordFont.setTextColor(context.getColor(R.color.default_color));
        holder.passwordName.setText(R.string.copied);
        holder.passwordName.setTextColor(context.getColor(R.color.white));

        SecureEncryptionHelper secureEncryptionHelper = new SecureEncryptionHelper(context);
        String decryptedPassword = secureEncryptionHelper.decrypt(password);

        ClipboardHelper.copyToClipboard(context, decryptedPassword);
        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();
    }

    public interface OnItemClickListener {
        void onItemClick(PasswordItem item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout rootView;
        private final TextView passwordFont, passwordName;
        private final ImageView copyIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.rootView);
            passwordFont = itemView.findViewById(R.id.passwordFont);
            passwordName = itemView.findViewById(R.id.passwordName);
            copyIcon = itemView.findViewById(R.id.copyIcon);
        }
    }
}
