package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private ProgressDialog pd;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private GoogleSignInClient mGoogleSignInClient;
    private ValueEventListener setUserData;
    Trace myTrace;

    private Date currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        //progrress dialogue for loading
        pd = new ProgressDialog(LoginActivity.this);
        pd.setMessage("loading");
        pd.setCancelable(false);

        //google sign in options for signInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //google sign in client
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        //account holds the google account, this value can be used if user is previously signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        //sign in button
        SignInButton google = findViewById(R.id.google);

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signIn();

            }
        });

        userRef= FirebaseDatabase.getInstance().getReference("Users");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("updateui","onstart calling updateui for transition");

        updateUI();
    }


    void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);
        pd.show();

    }


    //when user selects his google account
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        pd.dismiss();
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 9001) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
            // Signed in successfully, show authenticated UI.

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("abc", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        String personName = acct.getDisplayName();
        //  String personGivenName = acct.getGivenName();
        //   String personFamilyName = acct.getFamilyName();
        String personEmail = acct.getEmail();
        Uri photoUri = acct.getPhotoUrl();

        String photo = photoUri.toString();

        Log.d("abc", "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        //showProgressDialog();
        // [END_EXCLUDE]
        pd.show();

        //generate credentials form the Google sign in account for firebase sign in with credentials
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        //firebase sign in
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d("updateui","auth complete listener is being called");

                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Bibliofy Authentication Failed.", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                            mGoogleSignInClient.signOut();
                        }
                        else{
                            updateUI();

                        }

                    }
                });
    }

    void updateUI(){

        //if firebase auth is not null goto dashboard
        if (mAuth.getCurrentUser() != null ) {
            Log.d("updateui","mauth exists updateUI is getting called");


            if (pd.isShowing())
                pd.dismiss();
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();

        }
        else
            Log.d("updateui","mauth is null updateUI is getting called");

    }

}
