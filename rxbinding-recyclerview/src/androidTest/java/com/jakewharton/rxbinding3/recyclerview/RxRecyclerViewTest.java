package com.jakewharton.rxbinding3.recyclerview;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.jakewharton.rxbinding.ViewDirtyIdlingResource;
import com.jakewharton.rxbinding2.RecordingObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public final class RxRecyclerViewTest {
  @Rule public final ActivityTestRule<RxRecyclerViewTestActivity> activityRule =
      new ActivityTestRule<>(RxRecyclerViewTestActivity.class);

  private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

  RecyclerView view;
  private ViewDirtyIdlingResource viewDirtyIdler;
  private View child;

  @Before public void setUp() {
    RxRecyclerViewTestActivity activity = activityRule.getActivity();
    view = activity.recyclerView;
    child = new View(activityRule.getActivity());
    viewDirtyIdler = new ViewDirtyIdlingResource(activity);
    Espresso.registerIdlingResources(viewDirtyIdler);
  }

  @After public void tearDown() {
    Espresso.unregisterIdlingResources(viewDirtyIdler);
  }

  @Test public void childAttachEvents() {
    RecordingObserver<RecyclerViewChildAttachStateChangeEvent> o = new RecordingObserver<>();
    RxRecyclerView.childAttachStateChangeEvents(view)
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(o);
    o.assertNoMoreEvents();

    final SimpleAdapter adapter = new SimpleAdapter(child);

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.setAdapter(adapter);
      }
    });
    assertEquals(new RecyclerViewChildAttachEvent(view, child), o.takeNext());

    o.dispose();

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.setAdapter(adapter);
      }
    });

    o.assertNoMoreEvents();
  }

  @Test public void childDetachEvents() {
    final SimpleAdapter adapter = new SimpleAdapter(child);

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.setAdapter(adapter);
      }
    });

    RecordingObserver<RecyclerViewChildAttachStateChangeEvent> o = new RecordingObserver<>();
    RxRecyclerView.childAttachStateChangeEvents(view)
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(o);
    o.assertNoMoreEvents();

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.setAdapter(null);
      }
    });
    assertEquals(new RecyclerViewChildDetachEvent(view, child), o.takeNext());

    o.dispose();

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.setAdapter(adapter);
      }
    });

    o.assertNoMoreEvents();
  }

  @Test public void scrollEventsVertical() {
    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.setAdapter(new Adapter());
      }
    });

    RecordingObserver<RecyclerViewScrollEvent> o = new RecordingObserver<>();
    RxRecyclerView.scrollEvents(view)
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(o);
    o.assertNoMoreEvents();

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.scrollBy(0, 50);
      }
    });
    RecyclerViewScrollEvent event1 = o.takeNext();
    assertNotNull(event1);
    assertEquals(50, event1.getDy());

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.scrollBy(0, 0);
      }
    });
    o.assertNoMoreEvents();

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.scrollBy(0, -50);
      }
    });
    RecyclerViewScrollEvent event2 = o.takeNext();
    assertNotNull(event2);
    assertEquals(-50, event2.getDy());

    // Back at position 0. Trying to scroll earlier shouldn't fire any events
    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.scrollBy(0, -50);
      }
    });
    o.assertNoMoreEvents();

    o.dispose();

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.scrollBy(0, 50);
      }
    });
    o.assertNoMoreEvents();
  }

  @Test public void scrollEventsHorizontal() {
    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.setAdapter(new Adapter());
        ((LinearLayoutManager) view.getLayoutManager()).setOrientation(LinearLayoutManager.HORIZONTAL);
      }
    });

    instrumentation.waitForIdleSync();
    RecordingObserver<RecyclerViewScrollEvent> o = new RecordingObserver<>();
    RxRecyclerView.scrollEvents(view)
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(o);
    o.assertNoMoreEvents();

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.scrollBy(50, 0);
      }
    });
    RecyclerViewScrollEvent event3 = o.takeNext();
    assertNotNull(event3);
    assertEquals(50, event3.getDx());

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.scrollBy(0, 0);
      }
    });
    o.assertNoMoreEvents();

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.scrollBy(-50, 0);
      }
    });
    RecyclerViewScrollEvent event4 = o.takeNext();
    assertNotNull(event4);
    assertEquals(-50, event4.getDx());

    // Back at position 0. Trying to scroll earlier shouldn't fire any events
    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.scrollBy(-50, 0);
      }
    });
    o.assertNoMoreEvents();

    o.dispose();

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.scrollBy(50, 0);
      }
    });
    o.assertNoMoreEvents();
  }

  private class SimpleAdapter extends RecyclerView.Adapter {
    private final View child;

    SimpleAdapter(View child) {
      this.child = child;
    }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new RecyclerView.ViewHolder(child) {
      };
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    }

    @Override public int getItemCount() {
      return 1;
    }
  }

  private static class Adapter extends RecyclerView.Adapter<ViewHolder> {
    public Adapter() {
      setHasStableIds(true);
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
      TextView v = (TextView) LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
      return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
      holder.textView.setText(String.valueOf(position));
    }

    @Override public int getItemCount() {
      return 100;
    }

    @Override public long getItemId(int position) {
      return position;
    }
  }

  private static class ViewHolder extends RecyclerView.ViewHolder {

    TextView textView;

    ViewHolder(TextView itemView) {
      super(itemView);
      this.textView = itemView;
    }
  }
}
