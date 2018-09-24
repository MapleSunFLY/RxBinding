package com.jakewharton.rxbinding3.recyclerview;

import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.jakewharton.rxbinding2.RecordingObserver;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;

@RunWith(AndroidJUnit4.class)
public final class RxRecyclerViewAdapterTest {
  @Rule public final UiThreadTestRule uiThread = new UiThreadTestRule();

  private final TestRecyclerAdapter adapter = new TestRecyclerAdapter();

  @Test @UiThreadTest public void dataChanges() {
    RecordingObserver<Object> o = new RecordingObserver<>();
    RxRecyclerViewAdapter.dataChanges(adapter).subscribe(o);
    assertSame(adapter, o.takeNext());

    adapter.notifyDataSetChanged();
    assertSame(adapter, o.takeNext());

    adapter.notifyDataSetChanged();
    assertSame(adapter, o.takeNext());

    o.dispose();
    adapter.notifyDataSetChanged();
    o.assertNoMoreEvents();
  }

  private static final class TestRecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {
    TestRecyclerAdapter() {
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return null;
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
    }

    @Override public int getItemCount() {
      return 0;
    }
  }
}
