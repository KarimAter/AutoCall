package com.karim.ater.fajralarm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    // Todo: Finish
    private RecyclerItemTouchHelperListener listener;
    private Drawable icon;

    RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            final View foregroundView = ((ContactsAdapter.ContactViewHolder) viewHolder).mView;
            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((ContactsAdapter.ContactViewHolder) viewHolder).mView;
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((ContactsAdapter.ContactViewHolder) viewHolder).mView;
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        int backgroundCornerOffset = 0;
        final View foregroundView = ((ContactsAdapter.ContactViewHolder) viewHolder).mView;
        Context context = ((ContactsAdapter) recyclerView.getAdapter()).getContext();
        ColorDrawable background = new ColorDrawable(context.getResources().getColor(R.color.colorSecondary));

        Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_delete_black_24dp);
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX < 0) {
                int iconMargin = (foregroundView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconTop = foregroundView.getTop() + (foregroundView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + icon.getIntrinsicHeight();

                background.setBounds(foregroundView.getRight() + ((int) dX) - backgroundCornerOffset,
                        foregroundView.getTop(), foregroundView.getRight(), foregroundView.getBottom());

                int iconLeft = foregroundView.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconRight = foregroundView.getRight() - iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                background.draw(c);
                icon.draw(c);
            }
//        } else { // view is unSwiped
//            background.setBounds(0, 0, 0, 0);
//            icon.setBounds(0,0,0,0);
//        }

            else {

                icon.setBounds(0, 0, 0, 0);
                icon.draw(c);
            }
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    public interface RecyclerItemTouchHelperListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }
}
