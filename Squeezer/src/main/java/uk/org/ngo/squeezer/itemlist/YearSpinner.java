/*
 * Copyright (c) 2011 Kurt Aaholst <kaaholst@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.ngo.squeezer.itemlist;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import java.util.List;

import uk.org.ngo.squeezer.Util;
import uk.org.ngo.squeezer.framework.ItemAdapter;
import uk.org.ngo.squeezer.framework.ItemListActivity;
import uk.org.ngo.squeezer.model.Year;
import uk.org.ngo.squeezer.service.ISqueezeService;
import uk.org.ngo.squeezer.util.ImageFetcher;

public class YearSpinner {

    private static final String TAG = YearSpinner.class.getName();

    YearSpinnerCallback callback;

    private final ItemListActivity activity;

    private final Spinner spinner;

    public YearSpinner(YearSpinnerCallback callback, ItemListActivity activity, Spinner spinner) {
        this.callback = callback;
        this.activity = activity;
        this.spinner = spinner;
        registerCallback();
        orderItems(0);
    }

    private void orderItems(int start) {
        if (callback.getService() != null) {
            callback.getService().years(start);
        }
    }

    public void registerCallback() {
        if (callback.getService() != null) {
            callback.getService().registerYearListCallback(yearListCallback);
        }
    }

    public void unregisterCallback() {
        if (callback.getService() != null) {
            callback.getService().unregisterYearListCallback(yearListCallback);
        }
    }

    private final IServiceYearListCallback yearListCallback = new IServiceYearListCallback() {
        private ItemAdapter<Year> adapter;

        public void onYearsReceived(final int count, final int start, final List<Year> list) {
            callback.getUIThreadHandler().post(new Runnable() {
                public void run() {
                    if (adapter == null) {
                        YearView itemView = new YearView(activity) {
                            @Override
                            public View getAdapterView(View convertView, ViewGroup parent,
                                    Year item,
                                    ImageFetcher unused) {
                                return Util.getSpinnerItemView(getActivity(), convertView, parent,
                                        item.getName());
                            }

                            @Override
                            public View getAdapterView(View convertView, ViewGroup parent,
                                    String label) {
                                return Util.getSpinnerItemView(getActivity(), convertView, parent,
                                        label);
                            }
                        };
                        adapter = new ItemAdapter<Year>(itemView, true, null);
                        spinner.setAdapter(adapter);
                    }
                    adapter.update(count, start, list);
                    spinner.setSelection(adapter.findItem(callback.getYear()));

                    if (count > start + list.size()) {
                        if ((start + list.size()) % adapter.getPageSize() == 0) {
                            orderItems(start + list.size());
                        }
                    }
                }
            });
        }

    };

    public interface YearSpinnerCallback {

        ISqueezeService getService();

        Handler getUIThreadHandler();

        Year getYear();

        void setYear(Year year);
    }

}
