package com.dennis.usaficustomer;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;


import com.dennis.usaficustomer.Common.Common;
import com.dennis.usaficustomer.Model.Token;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {




    Button instantPickup;
    private TextView amount;
    private EditText eCost;
    private EditText elocation;
    private Button schedulePickup;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private homeFragmentListener listener;

    String currentUser;
    int inputText;
    ViewPager viewPager;



    public HomeFragment() {


        // Required empty public constructor
    }

    public interface homeFragmentListener{

        void onInputASent(CharSequence input);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child(Common.user_customer_tbl).child(currentUser);

        elocation = v.findViewById(R.id.home_pickup_address);
        schedulePickup = v.findViewById(R.id.btn_schedule_pickup);
        instantPickup = v.findViewById(R.id.btn_insta_pickup);

        eCost = v.findViewById(R.id.clothes_num);
        amount = v.findViewById(R.id.text3);

        eCost.addTextChangedListener(new TextWatcher() {

                                      public void afterTextChanged(Editable s) {
                                          //Convert the Text to String
                                          String editText = eCost.getText().toString();


                                          try {
                                              inputText = Integer.parseInt(editText);
                                          } catch (NumberFormatException e) {
                                              return;

                                          }
                                          inputText *= 25;
                                               if (inputText>=250&& inputText<=10000){
                                                   amount.setText("Ksh "+inputText);
                                               }
                                               else {
                                                   amount.setText("Ksh 250");
                                               }



                                      }

                                         public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                             // TODO Auto-generated method stub
                                         }

                                         public void onTextChanged(CharSequence s, int start, int before, int count) {
                                             // TODO Auto-generated method stub
                                         }

                                         });


        schedulePickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveUserLocation();


            }
        });

        instantPickup.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence input = eCost.getText();
                try {
                    listener.onInputASent(input);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                viewPager = (ViewPager) getActivity().findViewById(R.id.main_tabs_pager);
                viewPager.setCurrentItem(3);

            }
        });

        updateFirebaseToken();
        return v;
    }


    public void  updateEditText(CharSequence newText){
        eCost.setText(newText);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if( context instanceof homeFragmentListener ){
            listener = (homeFragmentListener) context;
        }else {
            throw new RuntimeException(context.toString() + "must implement homeFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }

    private void SaveUserLocation() {

        String location = elocation.getText().toString();

//        String editText = eCost.getText().toString();
//        if(TextUtils.isEmpty(editText) ){
//            eCost.requestFocus();
//            eCost.setError(" required to know number of clothes to pickup");
////            return;
//        }
        if(TextUtils.isEmpty(location) ){
            elocation.requestFocus();
            elocation.setError("location is required to know from where to pickup");
//            return;
        }



        else{
            //inserting info into Fdb
            HashMap userMap = new HashMap();
            userMap.put("location",location);

            userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Intent nextIntent = new Intent(getActivity(), UserRequestActivity.class);
                        startActivity(nextIntent);

                        Toast.makeText(getActivity(),"wait for the next step!", Toast.LENGTH_LONG).show();


                    }
                    else{
                        String messege = task.getException().getMessage();
                        Toast.makeText(getActivity(),"Attention: "+messege, Toast.LENGTH_LONG).show();


                    }
                }
            });
        }

    }
}
