/*
 * Copyright (C) 2012 Jamie McDonald
 * 
 * This file is part of MusicBrainz for Android.
 * 
 * MusicBrainz for Android is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * MusicBrainz for Android is distributed in the hope that it 
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MusicBrainz for Android. If not, see 
 * <http://www.gnu.org/licenses/>.
 */

package org.musicbrainz.mobile.fragment;

import java.util.List;

import org.musicbrainz.android.api.data.ReleaseStub;
import org.musicbrainz.mobile.MusicBrainzApp;
import org.musicbrainz.mobile.R;
import org.musicbrainz.mobile.activity.ReleaseActivity;
import org.musicbrainz.mobile.adapter.list.ReleaseStubAdapter;
import org.musicbrainz.mobile.dialog.ConfirmBarcodeDialog;
import org.musicbrainz.mobile.dialog.ConfirmBarcodeDialog.ConfirmBarcodeCallbacks;
import org.musicbrainz.mobile.intent.IntentFactory.Extra;
import org.musicbrainz.mobile.loader.SearchReleaseLoader;
import org.musicbrainz.mobile.loader.SubmitBarcodeLoader;
import org.musicbrainz.mobile.loader.result.AsyncResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class BarcodeSearchFragment extends Fragment implements OnEditorActionListener, OnClickListener,
        OnItemClickListener, OnItemLongClickListener, ConfirmBarcodeCallbacks {

    private static final int SEARCH_RELEASE_LOADER = 0;
    private static final int SUBMIT_BARCODE_LOADER = 1;

    private TextView barcodeText;
    private EditText searchBox;
    private ImageButton searchButton;
    private TextView instructions;
    private TextView noResults;
    private ListView matches;
    private View loading;
    private View error;

    private String barcode;
    private List<ReleaseStub> results;
    private int selection = 0;
    
    private LoadingCallbacks loadingCallbacks;
    
    public BarcodeSearchFragment() {
        setRetainInstance(true);
    }
    
    public interface LoadingCallbacks {
        public void startLoading();
        public void stopLoading();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        barcode = activity.getIntent().getStringExtra(Extra.BARCODE);
        try {
            loadingCallbacks = (LoadingCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + LoadingCallbacks.class.getSimpleName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_barcode_search, container, false);
        findViews(layout);
        setListeners();
        barcodeText.setText(barcodeText.getText() + " " + barcode);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getLoaderManager().getLoader(SEARCH_RELEASE_LOADER) != null) {
            getLoaderManager().initLoader(SEARCH_RELEASE_LOADER, null, searchCallbacks);
        }
    }

    private void findViews(View layout) {
        searchBox = (EditText) layout.findViewById(R.id.barcode_search);
        barcodeText = (TextView) layout.findViewById(R.id.barcode_text);
        searchButton = (ImageButton) layout.findViewById(R.id.barcode_search_btn);
        matches = (ListView) layout.findViewById(R.id.barcode_list);
        instructions = (TextView) layout.findViewById(R.id.barcode_instructions);
        noResults = (TextView) layout.findViewById(R.id.noresults);
        loading = layout.findViewById(R.id.loading);
        error = layout.findViewById(R.id.error);
    }

    private void setListeners() {
        searchBox.setOnEditorActionListener(this);
        searchButton.setOnClickListener(this);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (v.getId() == R.id.barcode_search && actionId == EditorInfo.IME_NULL) {
            doSearch();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        doSearch();
    }

    private void doSearch() {
        String term = searchBox.getText().toString();
        if (term.length() != 0) {
            hideKeyboard();
            prepareSearch();
            getLoaderManager().destroyLoader(SEARCH_RELEASE_LOADER);
            getLoaderManager().initLoader(SEARCH_RELEASE_LOADER, null, searchCallbacks);
        } else {
            Toast.makeText(MusicBrainzApp.getContext(), R.string.toast_search_err, Toast.LENGTH_SHORT).show();
        }
    }

    private void prepareSearch() {
        searchButton.setEnabled(false);
        instructions.setVisibility(View.INVISIBLE);
        loading.setVisibility(View.VISIBLE);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) MusicBrainzApp.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selection = position;
        showSubmitDialog();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Intent releaseIntent = new Intent(getActivity(), ReleaseActivity.class);
        releaseIntent.putExtra(Extra.RELEASE_MBID, results.get(position).getReleaseMbid());
        startActivity(releaseIntent);
        return true;
    }
    
    private void showSubmitDialog() {
        DialogFragment submitDialog = new ConfirmBarcodeDialog();
        submitDialog.show(getFragmentManager(), ConfirmBarcodeDialog.TAG);
    }

    private LoaderCallbacks<AsyncResult<List<ReleaseStub>>> searchCallbacks = new LoaderCallbacks<AsyncResult<List<ReleaseStub>>>() {

        @Override
        public Loader<AsyncResult<List<ReleaseStub>>> onCreateLoader(int id, Bundle args) {
            return new SearchReleaseLoader(searchBox.getText().toString());
        }

        @Override
        public void onLoadFinished(Loader<AsyncResult<List<ReleaseStub>>> loader, AsyncResult<List<ReleaseStub>> data) {
            instructions.setVisibility(View.INVISIBLE);
            loading.setVisibility(View.INVISIBLE);
            switch (data.getStatus()) {
            case SUCCESS:
                handleSearchResults(data);
                break;
            case EXCEPTION:
                showConnectionErrorWarning();
            }
        }

        @Override
        public void onLoaderReset(Loader<AsyncResult<List<ReleaseStub>>> loader) {
            loader.reset();
        }
    };

    private void showConnectionErrorWarning() {
        matches.setAdapter(null);
        error.setVisibility(View.VISIBLE);
        Button retry = (Button) error.findViewById(R.id.retry_button);
        retry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.setVisibility(View.VISIBLE);
                error.setVisibility(View.GONE);
                getLoaderManager().restartLoader(SEARCH_RELEASE_LOADER, null, searchCallbacks);
            }
        });
    }

    private void handleSearchResults(AsyncResult<List<ReleaseStub>> result) {
        results = result.getData();
        matches.setAdapter(new ReleaseStubAdapter(getActivity(), R.layout.list_release, results));
        matches.setOnItemClickListener(this);
        matches.setOnItemLongClickListener(this);

        error.setVisibility(View.GONE);
        if (results.isEmpty()) {
            noResults.setVisibility(View.VISIBLE);
            matches.setVisibility(View.INVISIBLE);
        } else {
            matches.setVisibility(View.VISIBLE);
            noResults.setVisibility(View.INVISIBLE);
        }
        searchButton.setEnabled(true);
    }

    private LoaderCallbacks<AsyncResult<Void>> submissionCallbacks = new LoaderCallbacks<AsyncResult<Void>>() {

        @Override
        public Loader<AsyncResult<Void>> onCreateLoader(int id, Bundle args) {
            return new SubmitBarcodeLoader(getSelectedReleaseMbid(), barcode);
        }

        @Override
        public void onLoadFinished(Loader<AsyncResult<Void>> loader, AsyncResult<Void> data) {
            getLoaderManager().destroyLoader(SUBMIT_BARCODE_LOADER);
            loadingCallbacks.stopLoading();
            switch (data.getStatus()) {
            case EXCEPTION:
                Toast.makeText(MusicBrainzApp.getContext(), R.string.toast_barcode_fail, Toast.LENGTH_LONG).show();
                break;
            case SUCCESS:
                Toast.makeText(MusicBrainzApp.getContext(), R.string.toast_barcode, Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }

        @Override
        public void onLoaderReset(Loader<AsyncResult<Void>> loader) {
            loader.reset();
        }
    };

    private String getSelectedReleaseMbid() {
        return results.get(selection).getReleaseMbid();
    }
    
    @Override
    public ReleaseStub getCurrentSelection() {
        return results.get(selection);
    }

    @Override
    public void confirmSubmission() {
        loadingCallbacks.startLoading();
        getLoaderManager().initLoader(SUBMIT_BARCODE_LOADER, null, submissionCallbacks);
    }

}
