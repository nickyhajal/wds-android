package com.worlddominationsummit.wdsandroid;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.Response;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

/**
 * Created by nicky on 5/18/15.
 */
public class CartFragment extends Fragment {
    public View mView;
    public TextView mName;
    public TextView mDescr;
    public TextView mPrice;
    public TextView mCardDetails;
    public Button mScanCard;
    public Button mToggleCard;
    public Button mExpiration;
    public Button mSubmit;
    public EditText mCardNum;
    public EditText mCardCVV;
    public LinearLayout mExistingCardShell;
    public LinearLayout mNewCardShell;
    public String mCode;
    public int mExpMonth = 0;
    public int mExpYear = 0;
    public HashMap<String, String> mProd;
    public boolean mSyncCard;
    public boolean cardInitd = false;
    public boolean mCardExists = false;
    public boolean mUseExistingCard = true;
    public JSONObject mPurchData;
    public boolean mCharging = false;

    public void setProduct(String code, HashMap<String, String> prod) {
        mCode = code;
        mProd = prod;
    }

    public void updateCart() {
        if (mCode != null && mProd != null) {
            if (mCode.equals("academy")) {
                mName.setText(mProd.get("name"));
                mDescr.setText(mProd.get("descr"));
                mPrice.setText("$29");
                mPurchData = new JSONObject();
                try {
                    mPurchData.put("event_id", mProd.get("event_id"));
                } catch (JSONException e) {
                    Log.e("WDS", "Json Exception", e);
                }
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        updateCart();
        updateCard();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.cart, container, false);
            LinearLayout content = (LinearLayout) mView.findViewById(R.id.content);
            mName = (TextView) mView.findViewById(R.id.name);
            mDescr = (TextView) mView.findViewById(R.id.descr);
            mPrice = (TextView) mView.findViewById(R.id.price);
            mCardDetails = (TextView) mView.findViewById(R.id.existing_details);
            mCardNum = (EditText) mView.findViewById(R.id.cc_number);
            mCardCVV = (EditText) mView.findViewById(R.id.cc_cvv);
            mScanCard = (Button) mView.findViewById(R.id.scanCard);
            mToggleCard = (Button) mView.findViewById(R.id.toggleCard);
            mExpiration = (Button) mView.findViewById(R.id.cc_exp_open);
            mSubmit = (Button) mView.findViewById(R.id.submit);
            mExistingCardShell = (LinearLayout) mView.findViewById(R.id.existingCardShell);
            mNewCardShell = (LinearLayout) mView.findViewById(R.id.newCardShell);
            Font.applyTo(content);
            mScanCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startCardScan();
                }
            });
            mToggleCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleCard();
                }
            });
            mExpiration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showExpirationPicker();
                }
            });
            mSubmit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    startPurchaseProcess();
                }
            });
        }
        return mView;
    }

    public void startPurchaseProcess() {
        if (mCharging) {
            return;
        }
        mCharging = true;
        mSubmit.setText("Processing...");
        if (mUseExistingCard) {
            chargeCard(Me.atn.card.optString("hash"));
        } else {
            Card card = new Card(mCardNum.getText().toString(), mExpMonth, mExpYear, mCardCVV.getText().toString());
            String errBody = "";
            if (!card.validateNumber()) {
                errBody = "Hey, it seems like your credit card number isn't valid. Can you double-check it?";
            } else if (!card.validateExpiryDate()) {
                errBody = "Hmm, it looks like your credit card's expiration date isn't valid. Can you double-check it?";
            } else if (!card.validateCVC()) {
                errBody = "Hmm, it looks like your CVV isn't valid. Can you double-check it?";
            }
            if (errBody.length() > 0) {
                showError(errBody);
            }
            else {
                try {
                    // pk_test_8WKTIWKXB6T1eFT9sFqrymCM
                    Stripe stripe = new Stripe("pk_live_v32iH6nfQOgPmKgQiNOrnZCi");
                    stripe.createToken(card,
                            new TokenCallback() {
                                public void onSuccess(Token token) {
                                    chargeCard(token.getId());
                                }

                                public void onError(Exception error) {
                                    String err = error.getLocalizedMessage();
                                    String b = "Hm, it looks like there was a problem: \n\n" + err;
                                    showError(b);
                                }
                            }
                    );
                } catch (AuthenticationException e) {
                    Log.e("WDS", "Json Exception", e);
                }
            }
        }
    }

    public void chargeCard(String token) {
        JSONObject params = new JSONObject();
        try {
            params.put("card_id", token);
            params.put("code", mCode);
            params.put("via", "and");
            params.put("purchase_data", mPurchData);
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        Api.post("product/charge", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Puts.i(response);
                if (response.has("err")) {
                    showError();
                }
                else {
                    mSubmit.setText("Success!");
                    Me.addRsvp(mProd.get("event_id"));
                    if (MainActivity.self.eventsFragment != null && MainActivity.self.eventsFragment == MainActivity.self.active) {
                        MainActivity.self.eventsFragment.update_items();
                    }
                    if (MainActivity.self.eventFragment != null && MainActivity.self.eventFragment == MainActivity.self.active) {
                        MainActivity.self.eventFragment.updateRsvpButton();
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mCharging = false;
                            getFragmentManager().popBackStackImmediate();
                            mSubmit.setText("Purchase");
                            syncCard();
                        }
                    }, 4000);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showError();
            }
        });
    }

    public void syncCard() {
        Me.sync(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                cardInitd = false;
                updateCard();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    private void updateCard() {
        if (cardInitd) {
            return;
        }
        cardInitd = true;
        if (Me.atn.card != null && Me.atn.card.has("last4")) {
            JSONObject c = Me.atn.card;
            mExistingCardShell.setVisibility(View.VISIBLE);
            mNewCardShell.setVisibility(View.GONE);
            mCardDetails.setText(c.optString("brand")+" ending in "+c.optString("last4")
                +"\n"+"Exp: "+c.optString("exp_month")+"/"+c.optString("exp_year"));
            mToggleCard.setVisibility(View.VISIBLE);
            mToggleCard.setText("Use New Card");
            mCardExists = true;
            mUseExistingCard = true;
        } else {
            mExistingCardShell.setVisibility(View.GONE);
            mNewCardShell.setVisibility(View.VISIBLE);
            mToggleCard.setVisibility(View.GONE);
            mCardExists = false;
            mUseExistingCard = false;
        }
    }
    private void toggleCard() {
        if (mUseExistingCard) {
            mUseExistingCard = false;
            mExistingCardShell.setVisibility(View.GONE);
            mNewCardShell.setVisibility(View.VISIBLE);
            mToggleCard.setText("Use Existing Card");
        } else {
            mUseExistingCard = true;
            mExistingCardShell.setVisibility(View.VISIBLE);
            mNewCardShell.setVisibility(View.GONE);
            mToggleCard.setText("Use New Card");
        }
    }
    private void startCardScan() {
        Intent scanIntent = new Intent(MainActivity.self, CardIOActivity.class);

        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, false); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_KEEP_APPLICATION_THEME, true); // default: false

        // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
        MainActivity.self.startActivityForResult(scanIntent, MainActivity.self.SCAN_REQUEST_CODE);
    }
    public void setCard(String num, String cvv, int m, int y) {
        setExpiration((m-1), y);
        mCardCVV.setText(cvv);
        mCardNum.setText(num);
    }

    public void setExpiration(int month, int year) {
        mExpMonth = month+1;
        mExpYear = year;
        String m = String.valueOf(mExpMonth);
        if (m.length() == 1) {
            m = "0"+m;
        }
        String str = "Exp: "+m+"/"+mExpYear;
        mExpiration.setText(str);
        mExpiration.setTextColor(getResources().getColor(R.color.blue));
    }
    public void showExpirationPicker() {
        ExpirationPicker newFragment = new ExpirationPicker();
        if (mExpYear > 0) {
            newFragment.setDefs(mExpMonth-1, mExpYear);
        }
        newFragment.show(MainActivity.self.getSupportFragmentManager(), "datePicker");
    }

    public void showError() {
        showError("Looks like there was a problem. Can you try again? If you continue to have trouble, please try another card.");
    }
    public void showError(String errBody) {
        final ContentDialog dialog = new ContentDialog();
        dialog.setContent("There Was a Problem", errBody, "Got it!");
        dialog.show(MainActivity.self.getFragmentManager(), "carterrordialog");
        mSubmit.setText("There Was a Problem");
        mCharging = false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSubmit.setText("Purchase");
            }
        }, 4000);
    }
}
