package com.example.workflow_s.ui.taskdetail.dialog.package_dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.core.content.FileProvider;
import android.view.View;
import android.widget.Button;

import com.example.workflow_s.R;
import com.example.workflow_s.utils.ImageUtils;

import java.io.File;
import java.io.IOException;

/**
 * Workflow_S
 * Created by TinhPV on 2019-07-16
 * Copyright © 2019 TinhPV. All rights reserved
 **/


public class ImageDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private final int REQUEST_CAMERA = 1, REQUEST_GALLERY = 2;

    View view;
    Button cameraButton, galleryButton;
    String currentPhotoPath;

    public interface ClickEventListener {
        void onFinishedPickingImages(String imagePath, Uri imageData);
    }

    ClickEventListener mListener;


    public static ImageDialogFragment newInstance() {
        return new ImageDialogFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (ClickEventListener) getTargetFragment();
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        view = View.inflate(getContext(), R.layout.fragment_checklistdetail_dialog_image, null);
        dialog.setContentView(view);
        ((View) view.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
        cameraButton = view.findViewById(R.id.bt_camera_pick);
        cameraButton.setOnClickListener(this);
        galleryButton = view.findViewById(R.id.bt_gallery_pick);
        galleryButton.setOnClickListener(this);
    }

    private void dispatchGetPictureFromGalleryIntent() {
        Intent intentGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intentGallery.setType("image/*");
        startActivityForResult(intentGallery, REQUEST_GALLERY);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = ImageUtils.createImageFile();
                currentPhotoPath = photoFile.getPath();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.workflow_s",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                mListener.onFinishedPickingImages(currentPhotoPath, null);
            } else if(requestCode == REQUEST_GALLERY) {
                Uri pickedImage = data.getData();
                mListener.onFinishedPickingImages(null, pickedImage);
            } // end if
        }

        dismiss();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_camera_pick) {
            dispatchTakePictureIntent();
        } else {
            dispatchGetPictureFromGalleryIntent();
        }
    }
}
