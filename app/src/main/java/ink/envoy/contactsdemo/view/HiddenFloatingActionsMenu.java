package ink.envoy.contactsdemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class HiddenFloatingActionsMenu extends FloatingActionsMenu {

    private boolean isShown = true;
    private int ANIM_DURATION = 300;
    private boolean mVisible = false;

    public HiddenFloatingActionsMenu(Context context) {
        super(context);
    }

    public HiddenFloatingActionsMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HiddenFloatingActionsMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void show(boolean isVisible) {
        mVisible = isVisible;
        int translationX = isVisible ? 0 : (getWidth()/2) + getMarginRight();
        this.animate().translationX(translationX).setDuration(ANIM_DURATION).start();
    }

    public boolean isShown() {
        return isShown;
    }

    private int getMarginRight() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((MarginLayoutParams) layoutParams).rightMargin;
        }
        return marginBottom;
    }

    public boolean getVisible(){
        return mVisible;
    }

}
