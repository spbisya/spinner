package com.leprefox.fidgetspinner;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.leprefox.fidgetspinner.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.leprefox.fidgetspinner.utils.Utils.getAngle;
import static com.leprefox.fidgetspinner.utils.Utils.getQuadrant;

public class SpinnerActivity extends AppCompatActivity {
    @BindView(R.id.spinner)
    ImageView spinner;

    private GestureDetector detector;
    private boolean[] quadrantTouched;
    private ObjectAnimator rotationAnimator;
    int DURATION_FOR_ONE_ROTATION = 250;
    private int spinnerHeight, spinnerWidth;
    private static Bitmap imageOriginal, imageScaled;
    private static Matrix matrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spinner);
        ButterKnife.bind(this);
        setImage(R.drawable.spinner);
        spinner.setOnTouchListener(new MyOnTouchListener());
        spinner.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // method called more than once, but the values only need to be initialized one time
                if (spinnerHeight == 0 || spinnerWidth == 0) {
                    spinnerHeight = spinner.getHeight();
                    spinnerWidth = spinner.getWidth();

                    // resize
                    Matrix resize = new Matrix();
                    resize.postScale((float) Math.min(spinnerWidth, spinnerHeight) / (float) imageOriginal.getWidth(), (float) Math.min(spinnerWidth, spinnerHeight) / (float) imageOriginal.getHeight());
                    imageScaled = Bitmap.createBitmap(imageOriginal, 0, 0, imageOriginal.getWidth(), imageOriginal.getHeight(), resize, false);

                    // translate to the image view's center
                    float translateX = spinnerWidth / 2 - imageScaled.getWidth() / 2;
                    float translateY = spinnerHeight / 2 - imageScaled.getHeight() / 2;
                    matrix.postTranslate(translateX, translateY);

                    spinner.setImageBitmap(imageScaled);
                    spinner.setImageMatrix(matrix);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        if (rotationAnimator != null && rotationAnimator.isRunning()) {
            rotationAnimator.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (rotationAnimator != null && rotationAnimator.isPaused()) {
            rotationAnimator.resume();
        }
        super.onResume();
    }

    public void setImage(int imageId) {
        imageOriginal = BitmapFactory.decodeResource(getResources(), imageId);
        matrix = new Matrix();
        detector = new GestureDetector(this, new MyGestureDetector());
        // there is no 0th quadrant, to keep it simple the first value gets ignored
        quadrantTouched = new boolean[]{false, false, false, false, false};
        spinnerHeight = spinner.getHeight();
        spinnerWidth = spinner.getWidth();
        // resize
        Matrix resize = new Matrix();
        resize.postScale((float) Math.min(spinnerWidth, spinnerHeight) / (float) imageOriginal.getWidth(), (float) Math.min(spinnerWidth, spinnerHeight) / (float) imageOriginal.getHeight());
        imageScaled = Bitmap.createBitmap(imageOriginal, 0, 0, imageOriginal.getWidth(), imageOriginal.getHeight(), resize, false);

        // translate to the image view's center
        float translateX = spinnerWidth / 2 - imageScaled.getWidth() / 2;
        float translateY = spinnerHeight / 2 - imageScaled.getHeight() / 2;
        matrix.postTranslate(translateX, translateY);

        spinner.setImageBitmap(imageScaled);
        spinner.setImageMatrix(matrix);
    }

    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            // get the quadrant of the start and the end of the fling
            int q1 = getQuadrant(e1.getX() - (spinnerWidth / 2), spinnerHeight - e1.getY() - (spinnerHeight / 2));
            int q2 = getQuadrant(e2.getX() - (spinnerWidth / 2), spinnerHeight - e2.getY() - (spinnerHeight / 2));

            float velocity = (velocityX + velocityY) * 3;

            int rotationTime = (int) (velocity / 360) * DURATION_FOR_ONE_ROTATION;

            // the inversed rotations
            if ((q1 == 2 && q2 == 2 && Math.abs(velocityX) < Math.abs(velocityY))
                    || (q1 == 3 && q2 == 3)
                    || (q1 == 1 && q2 == 3)
                    || (q1 == 4 && q2 == 4 && Math.abs(velocityX) > Math.abs(velocityY))
                    || ((q1 == 2 && q2 == 3) || (q1 == 3 && q2 == 2))
                    || ((q1 == 3 && q2 == 4) || (q1 == 4 && q2 == 3))
                    || (q1 == 2 && q2 == 4 && quadrantTouched[3])
                    || (q1 == 4 && q2 == 2 && quadrantTouched[3])) {
                rotateWheelToTarget(spinner, -1 * velocity, Utils.getUnsignedInt(rotationTime));
            } else {
                rotateWheelToTarget(spinner, velocity, Utils.getUnsignedInt(rotationTime));
            }

            return true;
        }
    }

    public void rotateWheelToTarget(View view, float endDegree, int duration) {
        if (rotationAnimator != null) {
            rotationAnimator.cancel();
        }
        rotationAnimator = ObjectAnimator.ofFloat(view, "rotation", 0, endDegree);
        rotationAnimator.setDuration(duration);
        rotationAnimator.setInterpolator(new DecelerateInterpolator());
        rotationAnimator.start();
    }

    /**
     * Simple implementation of an {@link View.OnTouchListener} for registering the dialer's touch events.
     */
    private class MyOnTouchListener implements View.OnTouchListener {

        private double startAngle;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (rotationAnimator != null && rotationAnimator.isRunning()) {
                rotationAnimator.cancel();
            } else {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // reset the touched quadrants
                        for (int i = 0; i < quadrantTouched.length; i++) {
                            quadrantTouched[i] = false;
                        }
                        startAngle = getAngle(event.getX(), event.getY(), spinnerWidth, spinnerHeight);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        double currentAngle = getAngle(event.getX(), event.getY(), spinnerWidth, spinnerHeight);
                        rotateDialer((float) (startAngle - currentAngle));
                        startAngle = currentAngle;
                        break;
                }
            }

            // set the touched quadrant to true
            quadrantTouched[getQuadrant(event.getX() - (spinnerWidth / 2), spinnerHeight - event.getY() - (spinnerHeight / 2))] = true;

            detector.onTouchEvent(event);

            return true;
        }
    }


    /**
     * Rotate the dialer.
     *
     * @param degrees The degrees, the dialer should get rotated.
     */
    private void rotateDialer(float degrees) {
        matrix.postRotate(degrees, spinnerWidth / 2, spinnerHeight / 2);
        spinner.setImageMatrix(matrix);
    }


}
