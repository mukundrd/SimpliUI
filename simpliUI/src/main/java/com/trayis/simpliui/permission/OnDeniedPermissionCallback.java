package com.trayis.simpliui.permission;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;

import com.trayis.simpliui.R;

/**
 * Created by mukundrd on 6/8/17.
 */

public class OnDeniedPermissionCallback {

    private final ViewGroup rootView;
    private final String text;
    private final String buttonText;
    private final View.OnClickListener onButtonClickListener;
    private final Snackbar.Callback snackbarCallback;

    /**
     * @param rootView              Parent view to show the snackbar
     * @param text                  Message displayed in the snackbar
     * @param buttonText            Message displayed in the snackbar button
     * @param onButtonClickListener Action performed when the user clicks the snackbar button
     */
    private OnDeniedPermissionCallback(ViewGroup rootView, String text, String buttonText,
                                       View.OnClickListener onButtonClickListener, Snackbar.Callback snackbarCallback) {
        this.rootView = rootView;
        this.text = text;
        this.buttonText = buttonText;
        this.onButtonClickListener = onButtonClickListener;
        this.snackbarCallback = snackbarCallback;
    }

    public void onPermissionDenied() {
        String error = rootView.getContext().getString(R.string.snackbarText, text);
        Spanned snackbarMessage;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            snackbarMessage = Html.fromHtml(error);
        } else {
            snackbarMessage = Html.fromHtml(error, Html.FROM_HTML_MODE_LEGACY);
        }
        Snackbar snackbar = Snackbar.make(rootView, snackbarMessage, Snackbar.LENGTH_LONG);
        if (buttonText != null && onButtonClickListener != null) {
            snackbar.setAction(buttonText, onButtonClickListener);
            snackbar.setActionTextColor(ContextCompat.getColor(rootView.getContext(), android.R.color.holo_orange_light));
        }
        if (snackbarCallback != null) {
            snackbar.setCallback(snackbarCallback);
        }
        snackbar.show();
    }

    /**
     * Builder class to configure the displayed snackbar
     * Non set fields will not be shown
     */
    public static class Builder {
        private final ViewGroup rootView;
        private final String text;
        private String buttonText;
        private View.OnClickListener onClickListener;
        private Snackbar.Callback snackbarCallback;

        private Builder(ViewGroup rootView, String text) {
            this.rootView = rootView;
            this.text = text;
        }

        public static Builder with(ViewGroup rootView, String text) {
            return new Builder(rootView, text);
        }

        public static Builder with(ViewGroup rootView, @StringRes int textResourceId) {
            return Builder.with(rootView, rootView.getContext().getString(textResourceId));
        }

        /**
         * Adds a text button with the provided click deniedPermissionCallback
         */
        public Builder withButton(String buttonText, View.OnClickListener onClickListener) {
            this.buttonText = buttonText;
            this.onClickListener = onClickListener;
            return this;
        }

        /**
         * Adds a text button with the provided click deniedPermissionCallback
         */
        public Builder withButton(@StringRes int buttonTextResourceId,
                                  View.OnClickListener onClickListener) {
            return withButton(rootView.getContext().getString(buttonTextResourceId), onClickListener);
        }

        /**
         * Adds a button that opens the application settings when clicked
         */
        public Builder withOpenSettingsButton(String buttonText) {
            this.buttonText = buttonText;
            this.onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = rootView.getContext();
                    Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + context.getPackageName()));
                    myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                    myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(myAppSettings);
                }
            };
            return this;
        }

        /**
         * Adds a button that opens the application settings when clicked
         */
        public Builder withOpenSettingsButton(@StringRes int buttonTextResourceId) {
            return withOpenSettingsButton(rootView.getContext().getString(buttonTextResourceId));
        }

        /**
         * Adds a deniedPermissionCallback to handle the snackbar {@code onDismissed} and {@code onShown} events
         */
        public Builder withCallback(Snackbar.Callback callback) {
            this.snackbarCallback = callback;
            return this;
        }

        /**
         * Builds a new instance of {@link OnDeniedPermissionCallback}
         */
        public OnDeniedPermissionCallback build() {
            return new OnDeniedPermissionCallback(rootView, text, buttonText, onClickListener, snackbarCallback);
        }
    }
}
