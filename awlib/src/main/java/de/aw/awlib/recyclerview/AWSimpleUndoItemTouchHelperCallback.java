package de.aw.awlib.recyclerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import de.aw.awlib.R;
import de.aw.awlib.adapters.AWCursorDragDropRecyclerViewAdapter;

/**
 * Created by alex on 14.01.2017.
 */
public abstract class AWSimpleUndoItemTouchHelperCallback extends AWSimpleItemTouchHelperCallback {
    private final float ALPHA_FULL = 1.0f;
    private final Paint mPaint = new Paint();
    private Bitmap mIcon;

    /**
     * @param adapter
     *         AWCursorDragDropRecyclerViewAdapter
     */
    public AWSimpleUndoItemTouchHelperCallback(AWCursorDragDropRecyclerViewAdapter adapter) {
        super(adapter);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Get RecyclerView item from the ViewHolder
            Context mContext = recyclerView.getContext();
            mPaint.reset();
            if (mIcon == null) {
                mIcon = BitmapFactory
                        .decodeResource(mContext.getResources(), R.drawable.ic_action_discard);
            }
            View itemView = viewHolder.itemView;
            if (dX > 0) {

            /* Set your color for positive displacement */
                mPaint.setARGB(255, 255, 0, 0);
                // Draw Rect with varying right side, equal to displacement dX
                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                        (float) itemView.getBottom(), mPaint);
                // Set the image icon for Right swipe
                c.drawBitmap(mIcon, (float) itemView.getLeft(),
                        (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView
                                .getTop() - mIcon.getHeight()) / 2, mPaint);
            } else {

            /* Set your color for negative displacement */
                mPaint.setARGB(255, 0, 255, 0);
                // Draw Rect with varying left side, equal to the item's right side
                // plus negative displacement dX
                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                        (float) itemView.getRight(), (float) itemView.getBottom(), mPaint);
                //Set the image icon for Left swipe
                c.drawBitmap(mIcon, (float) itemView.getRight() - mIcon.getWidth(),
                        (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView
                                .getTop() - mIcon.getHeight()) / 2, mPaint);
            }
            // Fade out the view as it is swiped out of the parent's bounds
            final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
}
