package com.example.brunovsiq.mapchat.map;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.example.brunovsiq.mapchat.R;
import com.example.brunovsiq.mapchat.models.Partner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class UserListFragment extends Fragment {

    private ArrayList<Partner> partnerList =  new ArrayList<>();
    private ArrayList<String> usernameList =  new ArrayList<>();
    public static ListView usernameListView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        usernameListView = view.findViewById(R.id.username_list);

        AndroidNetworking.initialize(getActivity());
        //getPartnersList();

        return view;
    }



}
