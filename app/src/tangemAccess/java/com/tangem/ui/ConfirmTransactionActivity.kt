package com.tangem.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tangem.App
import com.tangem.Constant
import com.tangem.card_android.android.nfc.NfcLifecycleObserver
import com.tangem.card_android.android.reader.NfcManager
import com.tangem.card_android.data.EXTRA_TANGEM_CARD
import com.tangem.card_android.data.EXTRA_TANGEM_CARD_UID
import com.tangem.card_android.data.loadFromBundle
import com.tangem.card_common.data.TangemCard
import com.tangem.data.Blockchain
import com.tangem.di.ToastHelper
import com.tangem.ui.activity.PinRequestActivity
import com.tangem.ui.activity.SignTransactionActivity
import com.tangem.ui.event.TransactionFinishWithError
import com.tangem.util.UtilHelper
import com.tangem.wallet.CoinEngine
import com.tangem.wallet.CoinEngineFactory
import com.tangem.wallet.R
import com.tangem.wallet.TangemContext
import kotlinx.android.synthetic.tangemAccess.activity_confirm_transaction.*
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.util.*
import javax.inject.Inject

class ConfirmTransactionActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    @Inject
    internal lateinit var toastHelper: ToastHelper

    private lateinit var sp: SharedPreferences
    private lateinit var nfcManager: NfcManager
    private lateinit var ctx: TangemContext
    private lateinit var amount: CoinEngine.Amount

    private var isIncludeFee: Boolean = true
    private var requestPIN2Count = 0
    private var nodeCheck = true
    private var dtVerified: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_transaction)

        App.toastHelperComponent.inject(this)

        sp = PreferenceManager.getDefaultSharedPreferences(this)

        nfcManager = NfcManager(this, this)
        lifecycle.addObserver(NfcLifecycleObserver(nfcManager))

        ctx = TangemContext.loadFromBundle(this, intent.extras)

        val engine = CoinEngineFactory.create(ctx)

        @Suppress("DEPRECATION") val html = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(engine!!.balanceHTML, Html.FROM_HTML_MODE_LEGACY)
        else
            Html.fromHtml(engine!!.balanceHTML)
        tvBalance.text = html

        isIncludeFee = intent.getBooleanExtra(Constant.EXTRA_FEE_INCLUDED, true)

        if (isIncludeFee)
            tvIncFee.setText(R.string.confirm_transaction_including_fee)
        else
            tvIncFee.setText(R.string.confirm_transaction_not_including_fee)

        amount = CoinEngine.Amount(intent.getStringExtra(Constant.EXTRA_AMOUNT), intent.getStringExtra(Constant.EXTRA_AMOUNT_CURRENCY))

        if (engine.allowSelectFeeInclusion())
            tvIncFee.visibility = View.VISIBLE
        else
            tvIncFee.visibility = View.INVISIBLE

        if (ctx.card.blockchainID == Blockchain.Token.id) {
            // for Blockchain.Token limit decimals
            etAmount.setText(amount.toValueString(ctx.card.tokensDecimal))
        } else {
            // for others
            etAmount.setText(amount.toValueString())
        }

        tvCurrency.text = engine.balanceCurrency
        tvCurrency2.text = engine.feeCurrency
        tvCardID.text = ctx.card.cidDescription
        etWallet.setText(intent.getStringExtra(Constant.EXTRA_TARGET_ADDRESS))

        btnSend.visibility = View.INVISIBLE

        if (!engine.allowSelectFeeLevel()) {
            rgFee.visibility = View.INVISIBLE
        }

        etFee.isEnabled = sp.getBoolean(getString(R.string.pref_manual_editing_fee), false)

        // set listeners
        rgFee.setOnCheckedChangeListener { _, checkedId -> doSetFee(checkedId) }
        etFee.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    val eqFee = engine.evaluateFeeEquivalent(etFee!!.text.toString())
                    tvFeeEquivalent.text = eqFee

                    if (!ctx.coinData!!.amountEquivalentDescriptionAvailable) {
                        tvFeeEquivalent.error = getString(R.string.confirm_transaction_error_service_unavailable)
                        tvCurrency2.visibility = View.GONE
                        tvFeeEquivalent.visibility = View.GONE
                    } else
                        tvFeeEquivalent.error = null

                    if (sp.getBoolean(getString(R.string.pref_manual_editing_fee), false))
                        toastHelper.showSingleToast(this@ConfirmTransactionActivity, getString(R.string.confirm_transaction_warning_risk_delaying))

                } catch (e: Exception) {
                    e.printStackTrace()
                    tvFeeEquivalent.text = ""
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        btnSend.setOnClickListener {
            if (UtilHelper.isOnline(this)) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MINUTE, -1)

                if (dtVerified == null || dtVerified!!.before(calendar.time)) {
                    finishWithError(Activity.RESULT_CANCELED, getString(R.string.confirm_transaction_error_data_is_outdated))
                    return@setOnClickListener
                }

                val engineCoin = CoinEngineFactory.create(ctx)

                if (engineCoin!!.isNeedCheckNode && !nodeCheck) {
                    Toast.makeText(baseContext, getString(R.string.confirm_transaction_error_cannot_reach_node), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val txFee = engineCoin.convertToAmount(etFee.text.toString(), tvCurrency2.text.toString())
                val txAmount = engineCoin.convertToAmount(etAmount.text.toString(), tvCurrency.text.toString())

                if (!engineCoin.hasBalanceInfo()) {
                    finishWithError(Activity.RESULT_CANCELED, getString(R.string.confirm_transaction_error_cannot_check_balance))
                    return@setOnClickListener

                } else if (!engineCoin.isBalanceNotZero) {
                    finishWithError(Activity.RESULT_CANCELED, getString(R.string.general_wallet_empty))
                    return@setOnClickListener

                } else if (!engineCoin.isExtractPossible) {
                    finishWithError(Activity.RESULT_CANCELED, getString(R.string.confirm_transaction_error_incoming_transaction_unconfirmed))
                    return@setOnClickListener
                }

                if (!engineCoin.checkNewTransactionAmountAndFee(txAmount, txFee, isIncludeFee)) {
                    finishWithError(Activity.RESULT_CANCELED, getString(R.string.prepare_transaction_error_not_enough_funds))
                    return@setOnClickListener
                }

                requestPIN2Count = 0
                val intent = Intent(baseContext, PinRequestActivity::class.java)
                intent.putExtra(Constant.EXTRA_MODE, PinRequestActivity.Mode.RequestPIN2.toString())
                ctx.saveToIntent(intent)
                intent.putExtra(Constant.EXTRA_FEE_INCLUDED, isIncludeFee)
                startActivityForResult(intent, Constant.REQUEST_CODE_REQUEST_PIN2_)
            } else
                Toast.makeText(this, getString(R.string.general_error_no_connection), Toast.LENGTH_SHORT).show()
        }

        val coinEngine = CoinEngineFactory.create(ctx)

        progressBar.visibility = View.VISIBLE

        coinEngine!!.requestFee(
                object : CoinEngine.BlockchainRequestsCallbacks {
                    override fun onComplete(success: Boolean) {
                        if (success) {
                            onProgress()
                            progressBar.visibility = View.INVISIBLE
                            dtVerified = Date()
                        } else {
                            finishWithError(Activity.RESULT_CANCELED, ctx.error)
                        }
                    }

                    override fun onProgress() {
                        doSetFee(rgFee.checkedRadioButtonId)
                    }

                    override fun allowAdvance(): Boolean {
                        return UtilHelper.isOnline(this@ConfirmTransactionActivity)
                    }
                },
                etWallet.text.toString(),
                amount)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.REQUEST_CODE_SIGN_TRANSACTION) {
            if (data != null && data.extras != null) {
                if (data.extras!!.containsKey(EXTRA_TANGEM_CARD_UID) && data.extras!!.containsKey(EXTRA_TANGEM_CARD)) {
                    val updatedCard = TangemCard(data.getStringExtra(EXTRA_TANGEM_CARD_UID))
                    updatedCard.loadFromBundle(data.getBundleExtra(EXTRA_TANGEM_CARD))
                    ctx.card = updatedCard
                }
            }
            if (resultCode == Constant.RESULT_INVALID_PIN_ && requestPIN2Count < 2) {
                requestPIN2Count++

                val intent = Intent(baseContext, PinRequestActivity::class.java)
                intent.putExtra(Constant.EXTRA_MODE, PinRequestActivity.Mode.RequestPIN2.toString())
                ctx.saveToIntent(intent)
                intent.putExtra(Constant.EXTRA_FEE_INCLUDED, isIncludeFee)
                startActivityForResult(intent, Constant.REQUEST_CODE_REQUEST_PIN2_)

                return
            }
            setResult(resultCode, data)
            finish()
        } else if (requestCode == Constant.REQUEST_CODE_REQUEST_PIN2_) {
            if (resultCode == Activity.RESULT_OK) {
                val intent = Intent(baseContext, SignTransactionActivity::class.java)
                ctx.saveToIntent(intent)
                intent.putExtra(Constant.EXTRA_TARGET_ADDRESS, etWallet!!.text.toString())
                intent.putExtra(Constant.EXTRA_AMOUNT, etAmount.text.toString())
                intent.putExtra(Constant.EXTRA_AMOUNT_CURRENCY, tvCurrency.text.toString())
                intent.putExtra(Constant.EXTRA_FEE, etFee.text.toString())
                intent.putExtra(Constant.EXTRA_FEE_CURRENCY, tvCurrency2.text.toString())
                intent.putExtra(Constant.EXTRA_FEE_INCLUDED, isIncludeFee)
                startActivityForResult(intent, Constant.REQUEST_CODE_SIGN_TRANSACTION)
            } else
                Toast.makeText(baseContext, R.string.confirm_transaction_error_pin_2_is_required, Toast.LENGTH_LONG).show()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val intent = Intent()
                setResult(Activity.RESULT_CANCELED, intent)
                finish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onTagDiscovered(tag: Tag) {
        try {
            nfcManager.ignoreTag(tag)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun doSetFee(checkedRadioButtonId: Int) {
        var txtFee = ""
        when (checkedRadioButtonId) {
            R.id.rbMinimalFee ->
                if (ctx.coinData.minFee != null) {
                    txtFee = ctx.coinData.minFee!!.toValueString()
                    btnSend.visibility = View.VISIBLE
                } else
                    btnSend.visibility = View.INVISIBLE

            R.id.rbNormalFee ->
                if (ctx.coinData.normalFee != null) {
                    txtFee = ctx.coinData.normalFee!!.toValueString()
                    btnSend.visibility = View.VISIBLE
                } else
                    btnSend.visibility = View.INVISIBLE

            R.id.rbMaximumFee ->
                if (ctx.coinData.maxFee != null) {
                    txtFee = ctx.coinData.maxFee!!.toValueString()
                    btnSend.visibility = View.VISIBLE
                } else
                    btnSend.visibility = View.INVISIBLE
        }
        etFee.setText(txtFee.replace(',', '.'))
    }

    private fun finishWithError(errorCode: Int, message: String) {
        val transactionFinishWithError = TransactionFinishWithError()
        transactionFinishWithError.message = message
        EventBus.getDefault().post(transactionFinishWithError)

        val intent = Intent()
        intent.putExtra(Constant.EXTRA_MESSAGE, message)
        setResult(errorCode, intent)
        finish()
    }

}