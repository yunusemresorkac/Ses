package com.yeslabapps.sesly.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.google.common.collect.ImmutableList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.sesly.R
import com.yeslabapps.sesly.controller.DummyMethods
import com.yeslabapps.sesly.databinding.ActivityGetPremiumBinding
import com.yeslabapps.sesly.util.NetworkChangeListener
import java.util.HashMap


class GetPremiumActivity : AppCompatActivity() {

    private lateinit var binding : ActivityGetPremiumBinding

    private var billingClient: BillingClient? = null
    private var productDetails: ProductDetails? = null
    private var purchase: Purchase? = null
    private val premiumId = "get_premium"
    private val TAG_IAP = "InAppPurchaseTag"
    private  var firebaseUser :FirebaseUser? = null

    private val networkChangeListener = NetworkChangeListener()
    private lateinit var pd : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        firebaseUser = FirebaseAuth.getInstance().currentUser

        pd = ProgressDialog(this, R.style.CustomDialog)
        pd.setCancelable(false)
        pd.show()

        binding.toolbar.setNavigationOnClickListener { finish() }

        billingSetup()

        binding.buyBtn.setOnClickListener { makePurchase() }

    }

    private fun billingSetup() {
        billingClient = BillingClient.newBuilder(this@GetPremiumActivity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(
                @NonNull billingResult: BillingResult
            ) {
                if (billingResult.responseCode ==
                    OK
                ) {
                    Log.i(TAG_IAP, "OnBillingSetupFinish connected")
                    queryProduct()
                } else {
                    Log.i(TAG_IAP, "OnBillingSetupFinish failed")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.i(TAG_IAP, "OnBillingSetupFinish connection lost")
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun queryProduct() {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                ImmutableList.of(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(premiumId)
                        .setProductType(
                            BillingClient.ProductType.INAPP
                        )
                        .build()
                )
            )
            .build()
        billingClient!!.queryProductDetailsAsync(
            queryProductDetailsParams
        ) { _: BillingResult?, productDetailsList: List<ProductDetails> ->
            if (productDetailsList.isNotEmpty()) {
                productDetails = productDetailsList[0]
                runOnUiThread {
                    binding.buyBtn.isEnabled = true
                    val skuList: MutableList<String> = ArrayList()
                    skuList.add(premiumId)
                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                    billingClient!!.querySkuDetailsAsync(
                        params.build()
                    ) { billingResult1: BillingResult, list: List<SkuDetails>? ->
                        if (billingResult1.responseCode == OK && list != null) {
                            for (skuDetails in list) {
                                val price = skuDetails.price
                                println("fiyat $price")
                                binding.priceText.text = price
                            }
                        }
                    }
                    pd.dismiss()
                }
            } else {
                Log.i(TAG_IAP, "onProductDetailsResponse: No products")
                pd.dismiss()
            }
        }
    }

    private fun makePurchase() {
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                ImmutableList.of(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails!!)
                        .build()
                )
            )
            .build()
        billingClient!!.launchBillingFlow(this, billingFlowParams)
    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult: BillingResult, purchases: List<Purchase>? ->
            if (billingResult.responseCode ==
                OK
                && purchases != null
            ) {
                for (purchase in purchases) {
                    completePurchase(purchase)
                }
            } else if (billingResult.responseCode ==
                BillingClient.BillingResponseCode.USER_CANCELED
            ) {
                Log.i(TAG_IAP, "onPurchasesUpdated: Purchase Canceled")
                //StyleableToast.makeText(CompleteBuyActivity.this, "Bir hata oldu. Yeniden deneyin.", R.style.customToast).show();
            } else {
                Log.i(TAG_IAP, "onPurchasesUpdated: Error")

            }
        }

    private fun completePurchase(item: Purchase) {
        purchase = item
        if (purchase!!.purchaseState == Purchase.PurchaseState.PURCHASED) runOnUiThread {
            consumePurchase()
            FirebaseFirestore.getInstance().collection("Users").document(firebaseUser!!.uid)
                .update("userType",1).addOnSuccessListener {
                    binding.priceText.text = "Purchase Successful! You're Now Premium!"
                    val map: HashMap<String, Any> = HashMap()
                    val id = DummyMethods.generateRandomString(12)
                    map["time"] = System.currentTimeMillis()
                    map["id"] = id
                    map["userId"] = firebaseUser!!.uid
                    FirebaseFirestore.getInstance().collection("Buys").document(id)
                        .set(map)
                }

        }
    }

    private fun consumePurchase() {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase!!.purchaseToken)
            .build()
        val listener =
            ConsumeResponseListener { billingResult, purchaseToken ->
                if (billingResult.responseCode ==
                    OK
                ) {
                    runOnUiThread {}
                }
            }
        billingClient!!.consumeAsync(consumeParams, listener)
    }


    override fun onStart() {
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeListener, intentFilter)
        super.onStart()
    }

    override fun onStop() {
        unregisterReceiver(networkChangeListener)
        super.onStop()
    }





}