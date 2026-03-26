package com.example.myhealthy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView tvName = view.findViewById(R.id.tvProfileName);
        TextView tvEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvInfoName = view.findViewById(R.id.tvInfoName);
        TextView tvInfoEmail = view.findViewById(R.id.tvInfoEmail);
        TextView tvInfoProvider = view.findViewById(R.id.tvInfoProvider);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            String provider = "Email";

            if (user.getProviderData().size() > 1) {
                String providerId = user.getProviderData().get(1).getProviderId();
                if ("google.com".equals(providerId)) {
                    provider = "Google";
                }
            }

            tvName.setText(name != null && !name.isEmpty() ? name : "User");
            tvEmail.setText(email != null ? email : "-");
            tvInfoName.setText(name != null && !name.isEmpty() ? name : "-");
            tvInfoEmail.setText(email != null ? email : "-");
            tvInfoProvider.setText(provider);
        }

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}
