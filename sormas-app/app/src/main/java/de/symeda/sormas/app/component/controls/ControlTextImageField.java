package de.symeda.sormas.app.component.controls;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.databinding.BindingAdapter;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.symeda.sormas.app.R;

import de.symeda.sormas.api.task.TaskPriority;
import de.symeda.sormas.api.task.TaskStatus;

public class ControlTextImageField extends ControlTextReadField {

    // Constants

    public static final int DEFAULT_IMG_WIDTH = 24;
    public static final int DEFAULT_IMG_HEIGHT = 24;

    // Views

    protected ImageView imageView;

    // Attributes
    private int imageColor;
    private int imageWidth;
    private int imageHeight;

    // Other variables

    private int image;

    // Constructors

    public ControlTextImageField(Context context) {
        super(context);
    }

    public ControlTextImageField(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ControlTextImageField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Instance methods

    public void setImageBackground(int imageResource, int tintResource) {
        if (imageView != null){
            Context context = imageView.getContext();
            Drawable background = ContextCompat.getDrawable(context, imageResource);

            applyBackground(background, tintResource, context);
        }
    }

    public void setImageBackground(int tintResource) {
        if (imageView != null){
            Context context = imageView.getContext();
            Drawable background = ContextCompat.getDrawable(context, getImage());

            applyBackground(background, tintResource, context);
        }
    }

    private void applyBackground(Drawable background, int tintResource, Context context) {
        if (background != null) {
            background.setTint(context.getResources().getColor(tintResource));
        }

        imageView.setBackground(background);

        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.width = imageWidth;
        params.height = imageHeight;
    }

    // Overrides

    @Override
    protected void initialize(Context context, AttributeSet attrs, int defStyle) {
        super.initialize(context, attrs, defStyle);
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.ControlTextImageField,
                    0, 0);

            try {
                image = a.getResourceId(R.styleable.ControlTextImageField_image, R.drawable.blank);
                imageWidth = a.getDimensionPixelSize(R.styleable.ControlTextImageField_imageWidth, DEFAULT_IMG_WIDTH);
                imageHeight = a.getDimensionPixelSize(R.styleable.ControlTextImageField_imageHeight, DEFAULT_IMG_HEIGHT);
            } finally {
                a.recycle();
            }
        }
    }

    @Override
    protected void inflateView(Context context, AttributeSet attrs, int defStyle) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (inflater != null) {
            inflater.inflate(R.layout.control_textfield_image_layout, this);
        } else {
            throw new RuntimeException("Unable to inflate layout in " + getClass().getName());
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        imageView = (ImageView) this.findViewById(R.id.image);
    }

    // Data binding, getters & setters

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getImageColor() {
        return imageColor;
    }

    public void setImageColor(int imageColor) {
        this.imageColor = imageColor;
    }

    // Task priority
    @BindingAdapter(value = {"taskPriorityValue", "defaultValue"}, requireAll = false)
    public static void setTaskPriorityValue(ControlTextImageField textImageField, TaskPriority priority, String defaultValue) {
        if (priority == null) {
            textImageField.setValue(getDefaultValue(defaultValue));
        } else {
            textImageField.setValue(priority.toString());

            if (priority == TaskPriority.HIGH) {
                textImageField.setImageBackground(R.color.indicatorTaskPriorityHigh);
            } else if (priority == TaskPriority.NORMAL) {
                textImageField.setImageBackground(R.color.indicatorTaskPriorityNormal);
            } else if (priority == TaskPriority.LOW) {
                textImageField.setImageBackground(R.color.indicatorTaskPriorityLow);
            }
        }

        textImageField.setInternalValue(priority);
    }

    // Task status
    @BindingAdapter(value = {"taskStatusValue", "defaultValue"}, requireAll = false)
    public static void setTaskStatusValue(ControlTextImageField textImageField, TaskStatus status, String defaultValue) {
        if (status == null) {
            textImageField.setValue(getDefaultValue(defaultValue));
        } else {
            textImageField.setValue(status.toString());

            if (status == TaskStatus.PENDING) {
                textImageField.setImageBackground(R.color.indicatorTaskPending);
            } else if (status == TaskStatus.DONE) {
                textImageField.setImageBackground(R.color.indicatorTaskDone);
            } else if (status == TaskStatus.REMOVED) {
                textImageField.setImageBackground(R.color.indicatorTaskRemoved);
            } else if (status == TaskStatus.NOT_EXECUTABLE) {
                textImageField.setImageBackground(R.color.indicatorTaskNotExecutable);
            }
        }

        textImageField.setInternalValue(status);
    }

}