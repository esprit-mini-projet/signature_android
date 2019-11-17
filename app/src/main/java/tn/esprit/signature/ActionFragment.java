package tn.esprit.signature;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import tn.esprit.signature.databinding.FragmentActionBinding;


/**
 * A simple {@link Fragment} subclass.
 */
public class ActionFragment extends Fragment {
    private static final String TAG = "ActionFragment";

    private static final float CROSSHAIRE_RATIO = 1.72413793f;
    private static final int SAMPLE_WIDTH = 1000;
    private static final int SAMPLE_HEIGHT = 580;

    private Bitmap mBitmap;
    private FragmentActionBinding mDataBinding;
    private NameDialog dialog;
    private ProgressDialog progressDialog;


    public ActionFragment() {
        // Required empty public constructor
    }

    public static ActionFragment create(Bitmap bitmap) {
        ActionFragment actionFragment = new ActionFragment();
        actionFragment.mBitmap = bitmap;

        return actionFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_action, container, false);

        mDataBinding.ivBg.setImageBitmap(mBitmap);
        mDataBinding.multipleActions.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                mDataBinding.bgBottomFade.animate().alpha(1.0f).setDuration(200);
            }

            @Override
            public void onMenuCollapsed() {
                mDataBinding.bgBottomFade.animate().alpha(0.0f).setDuration(200);
            }
        });

        mDataBinding.actionCheck.setOnClickListener(view -> {
            DialogCallback callback = new DialogCallback() {
                @Override
                public void callback(@NotNull String name) {
                    File file = new File(
                            getActivity().getExternalFilesDir(null),
                            System.currentTimeMillis() + ".jpg"
                    );
                    try {
                        FileOutputStream output = new FileOutputStream(file);
                        extractBitmap().compress(Bitmap.CompressFormat.JPEG, 30, output);
                        output.close();

                        showProgress();

                        SignatureService.INSTANCE.checkSignature(file, name, new SignatureCallback() {

                            @Override
                            public void onSuccess(String name) {
                                dialog.dismiss();
                                hideProgress();
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_host, ResultFragment.Companion.newInstance(true))
                                        .addToBackStack("ActionFragment").commit();
                            }

                            @Override
                            public void onError(String msg) {
                                dialog.dismiss();
                                hideProgress();
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_host, ResultFragment.Companion.newInstance(false))
                                        .addToBackStack("ActionFragment").commit();
                            }
                        });
                    } catch (Exception e) {
                    }

                }
            };
            dialog = NameDialog.Companion.newInstance(callback);
            dialog.show(getActivity().getSupportFragmentManager(), "dialog name");
        });

        mDataBinding.actionRetake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mDataBinding.actionAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogCallback callback = new DialogCallback() {
                    @Override
                    public void callback(@NotNull String name) {
                        File file = new File(
                                getActivity().getExternalFilesDir(null),
                                System.currentTimeMillis() + ".jpg"
                        );
                        try {
                            FileOutputStream output = new FileOutputStream(file);
                            extractBitmap().compress(Bitmap.CompressFormat.JPEG, 30, output);
                            output.close();
                            showProgress();
                            SignatureService.INSTANCE.addSignature(file, name, new SignatureCallback() {

                                @Override
                                public void onSuccess(String name) {
                                    dialog.dismiss();
                                    hideProgress();
                                    Snackbar.make(getView(), "Signature has been added", Snackbar.LENGTH_LONG).show();
                                }

                                @Override
                                public void onError(String msg) {
                                    dialog.dismiss();
                                    hideProgress();
                                    Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
                                }
                            });
                        } catch (Exception e) {
                        }

                    }
                };
                dialog = NameDialog.Companion.newInstance(callback);
                dialog.show(getActivity().getSupportFragmentManager(), "dialog name");
            }
        });

        return mDataBinding.getRoot();
    }

    private void hideProgress() {
        progressDialog.hide();
    }

    private void showProgress() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please wait...");
        progressDialog.show();
    }

    private Bitmap extractBitmap() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        FrameLayout crosshair = mDataBinding.crosshair;

        int imageHeight = mBitmap.getHeight();
        int imageWidth = mBitmap.getWidth();
        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        float ratio = ((float) imageHeight) / displayHeight;

        displayWidth *= ratio;
        int crosshairX = (int) (crosshair.getX() * ratio);
        int crosshairY = (int) (crosshair.getY() * ratio);
        int crosshairWidth = (int) (crosshair.getWidth() * ratio);
        int crosshairHeight = (int) (crosshair.getHeight() * ratio);

        int leftWidthDiff = (imageWidth - displayWidth) / 2;
        Bitmap croppedBmp = Bitmap.createBitmap(mBitmap, leftWidthDiff + crosshairX, crosshairY, crosshairWidth, crosshairHeight);
        return Bitmap.createScaledBitmap(croppedBmp, 1000, 580, false);
    }

    private void saveBitmap(Bitmap bitmap) {
        FileOutputStream output = null;
        try {
            File file = new File(getActivity().getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
            output = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, output);
            //output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //mImage.close();
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
