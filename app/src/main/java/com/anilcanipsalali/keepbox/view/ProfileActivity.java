package com.anilcanipsalali.keepbox.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.anilcanipsalali.keepbox.BuildConfig;
import com.anilcanipsalali.keepbox.R;
import com.anilcanipsalali.keepbox.model.Profile;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private CircleImageView profileImage;
    private TextView nameTV, logoutTV, emailTV, language;
    private ImageView imageBack;
    private Button updateBTN;
    private ImageButton imageEditBTN;
    private DocumentReference docRef;
    private static final int IMAGE_PICKER = 1;
    private AlertDialog dialogLanguageChange;
    private Uri photoUri;
    private String imageUri;
    private ProgressDialog progressDialog;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();
        getDataFromCloud();
        clickListener();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        profileImage = findViewById(R.id.profileImage);
        nameTV = findViewById(R.id.nameTV);
        emailTV = findViewById(R.id.emailTV);
        logoutTV = findViewById(R.id.logoutTV);
        imageEditBTN = findViewById(R.id.imageEditBTN);
        language = findViewById(R.id.language);
        updateBTN = findViewById(R.id.updateBTN);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCancelable(false);
        TextView version = findViewById(R.id.version);
        version.setText("V"+BuildConfig.VERSION_CODE + ":" + BuildConfig.VERSION_NAME);

        //Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        docRef = db.collection("Users").document(user.getUid());
        imageBack = findViewById(R.id.imageBack);
    }

    private void getDataFromCloud() {
        docRef
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Profile profile = documentSnapshot.toObject(Profile.class);
                        assert profile != null;
                        nameTV.setText(profile.getName());
                        emailTV.setText(profile.getEmail());

                        Glide.with(ProfileActivity.this)
                                .load(profile.getImage())
                                .timeout(6000)
                                .placeholder(R.drawable.ic_profile)
                                .into(profileImage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void clickListener() {
        logoutTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                finish();
            }
        });

        imageEditBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(ProfileActivity.this)
                        .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                                if(multiplePermissionsReport.areAllPermissionsGranted()) {
                                    Intent intent = new Intent(Intent.ACTION_PICK);
                                    intent.setType("image/*");
                                    startActivityForResult(intent, IMAGE_PICKER);
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                            }
                        }).check();
            }
        });

        updateBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLanguageDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_PICKER && resultCode == RESULT_OK) {
            if(data != null) {
                photoUri = data.getData();
                updateBTN.setVisibility(View.VISIBLE);
            }
        }
    }

    private void uploadImage() {
        if(photoUri == null) {
            return;
        }
        String fileName = user.getUid()+".jpg";

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("Images/"+fileName);

        progressDialog.show();

        storageReference.putFile(photoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageUri = uri.toString();
                                    uploadImageURLToDatabase();
                                }
                            });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                long totalSi = snapshot.getTotalByteCount();
                long transferS = snapshot.getBytesTransferred();

                long totalSize = (totalSi / 1024);
                long transferSize = (transferS / 1024);

                progressDialog.setMessage(((int) transferSize) + "KB/ " + ((int) totalSize) + "KB'ı Yüklendi.");
            }
        });
    }

    private void uploadImageURLToDatabase() {
        Map<String, Object> map = new HashMap<>();
        map.put("image", imageUri);

        docRef
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateBTN.setVisibility(View.GONE);
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void showChangeLanguageDialog() {
        if(dialogLanguageChange == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_language_change,
                    (ViewGroup) findViewById(R.id.layoutLanguageChange)
            );
            builder.setView(view);

            dialogLanguageChange = builder.create();
            if(dialogLanguageChange.getWindow() != null) {
                dialogLanguageChange.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(R.id.textApply).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogLanguageChange.dismiss();
                }
            });

            view.findViewById(R.id.textCancelLNG).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialogLanguageChange.dismiss();
                }
            });
        }
        dialogLanguageChange.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDataFromCloud();
    }

}