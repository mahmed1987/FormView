package com.seed.widgets.formview

import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialAutoCompleteTextView
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.form_template.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import org.koin.ext.isInt


class FormView @JvmOverloads constructor(
    context: Context?,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialCardViewStyle
) : MaterialCardView(context, attributeSet, defStyleAttr), View.OnClickListener {


    private var smallSpace = 0
    private var mediumSpace = 0
    private var fragmentId = 0
    private var largeSpace = 0
    var formHasErrors = false
    private var formResource: Int? = null
    private var textFieldResource: Int = 0
    private var submitButtonFieldResource: Int = 0
    private val callbacks: FormCallbacks by lazy { attachCallbacks() as FormCallbacks }
    private val coroutineContext = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var form: Form


    init {
        View.inflate(
            context,
            R.layout.form_template,
            this
        ) // regionResourceId is not initialized here as yet
        smallSpace = resources.getDimensionPixelSize(R.dimen.unit_medium)
        mediumSpace = resources.getDimensionPixelSize(R.dimen.unit_medium)
        largeSpace = resources.getDimensionPixelSize(R.dimen.unit_large)
        context?.let {
            attributeSet?.let {
                studyAttributes(context, it)
            }
            designCard(context)
        }

    }

    private fun attachCallbacks(): FormCallbacks? {
        val ctx = when (context) {
            is AppCompatActivity -> (context as AppCompatActivity)
            is ContextThemeWrapper -> (((context as ContextThemeWrapper).baseContext) as AppCompatActivity)
            else -> null
        }
        return ctx?.let {
            if (fragmentId != 0) {
                //look for a formview with the id through all of the the fragments in this activity
                val fragment =
                    it.supportFragmentManager.findFragmentById(fragmentId)
                if (fragment != null) {
                    return when (fragment) {
                        is NavHostFragment -> fragment.childFragmentManager.primaryNavigationFragment
                        else -> fragment
                    } as FormCallbacks
                } else {
                    throw IllegalStateException("Nav Host Fragment was not found. Are you sure you are providing the correct id?")
                }
            }
            it as FormCallbacks
        }


    }


    private fun studyAttributes(context: Context, it: AttributeSet) {
        val typedArray = context.theme.obtainStyledAttributes(it, R.styleable.FormView, 0, 0)
        formResource = typedArray.getResourceId(R.styleable.FormView_form, R.raw.form)
        fragmentId = typedArray.getResourceId(R.styleable.FormView_fragmentId, 0)
        textFieldResource = typedArray.getResourceId(R.styleable.FormView_textfield, 0)
        submitButtonFieldResource = typedArray.getResourceId(R.styleable.FormView_submitButton, 0)

    }

    private fun designCard(context: Context) {
        formResource?.let {
            form = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .create()
                .fromJson(readJson(), Form::class.java)

            form.padding?.let {
                formContainer.setPadding(it.toPx(), it.toPx(), it.toPx(), it.toPx())
            }
            createForm()

        }
    }

    private fun readJson() =
        resources.openRawResource(R.raw.form).bufferedReader().use { it.readText() }

    //region Clicks
    override fun onClick(v: View?) {
    }

    //endregion
    //region Form
    private fun createForm() {
        form.run {
            title?.let { formTitle.text = title }
            rows.map { row ->
                createRow().apply {
                    this@FormView.formContainer.addView(this)
                    //does this row has more than one field?
                    row.fields.map {
                        if (it.weight != null)
                            this.addView(createField(it))
                        else
                            this@FormView.formContainer.addView(createField(it))
                    }
                }
            }

        }
    }

    private fun createRow() = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        layoutParams =
            ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    //endregion
    //region Fields
    private fun createField(field: Field): View {
        return when (field.type) {
            Type.TEXTFIELD -> createTextField(field)
            Type.BUTTON -> createButtonField(field)
            Type.SUBMIT_BUTTON_FIELD -> createSubmitButtonField(field)
            Type.DROPDOWN -> createDropDownField(field)
        }
    }

    //endregion
    //region TextField
    private fun createTextField(field: Field) =
        (inflate(if (textFieldResource != 0) textFieldResource else R.layout.form_textfield) as TextInputLayout).apply {
            val editText = this@apply.findViewById<EditText>(R.id.editText)
                ?: throw IllegalStateException("Your custom text field resource should have the TextInputEditText id as editText")

            hint = field.hint
            field.tag?.let { tag = it }
            addLayoutParams(mediumSpace, field.weight)

            field.inputType?.let {
                editText.inputType = when (it) {
                    InputType.NUMBER -> android.text.InputType.TYPE_CLASS_NUMBER
                    InputType.PHONE -> android.text.InputType.TYPE_CLASS_PHONE
                    InputType.TEXT_PASSWORD -> {
                        this@apply.isPasswordVisibilityToggleEnabled = true
                        android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

                    }
                }

            }
            field.validationRule?.let { validationRule ->
                isErrorEnabled = true
                editText.afterTextChanged { value ->
                    attachValidations(validationRule, value, field)
                }
                editText.setText("") // kick start validation
            }
        }


    //endregion
    //region ButtonField
    private fun createButtonField(field: Field) =
        (inflate(R.layout.form_button) as MaterialButton).apply {
            hint = field.hint
            addLayoutParams(mediumSpace, field.weight, field.layoutParam)
        }

    private fun View.addLayoutParams(
        space: Int,
        fieldWeight: Int? = null,
        layoutParam: LayoutParam? = null

    ): ViewGroup.LayoutParams {

        val width = if (layoutParam?.width?.name == "WRAP_CONTENT")
            LinearLayout.LayoutParams.WRAP_CONTENT
        else
            LinearLayout.LayoutParams.MATCH_PARENT

        return LinearLayout.LayoutParams(
            width,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            rightMargin = space
            leftMargin = space
            topMargin = space
            layoutParams = this
            fieldWeight?.let {
                weight = it.toFloat()
            }
            layoutParam?.gravity?.let {
                gravity = it.value
            }

        }
    }

    //endregion
    //region SubmitButtonField
    private fun createSubmitButtonField(field: Field) =
        (inflate(if (submitButtonFieldResource != 0) submitButtonFieldResource else R.layout.form_button) as MaterialButton).apply {
            hint = field.hint
            setOnClickListener {
                val result = produceResult()
                if (result.keys().iterator().asSequence().count() == form.rows.flatMap { it.fields }.filter { it.type != Type.SUBMIT_BUTTON_FIELD && it.type != Type.BUTTON }.count()) {
                    callbacks.stitchedResult(result.toString())
                }
            }
            addLayoutParams(mediumSpace, field.weight, field.layoutParam)
        }

    private fun produceResult() =
        JSONObject().apply {
            form
                .rows
                .flatMap { it.fields }
                .asSequence()
                .filter { it.type != Type.SUBMIT_BUTTON_FIELD && it.type != Type.BUTTON }
                .map { it.tag }
                .map { findViewWithTag<View>(it) as TextInputLayout }
                .filter { it.error == null }
                .map { view ->
                    Pair(
                        view.tag.toString(),
                        when (val view =
                            ((view.children.first() as FrameLayout).children.first())) {
                            is TextInputEditText -> {
                                view.text.toString()
                            }
                            is MaterialAutoCompleteTextView -> {
                                val dataList = view.tag as List<Pair<String, String>>
                                dataList.firstOrNull { it.second == view.text.toString() }?.first
                                    ?: ""
                            }
                            else -> {
                                ""
                            }

                        }
                    )
                }
                .toList()
                .map { pair -> put(pair.first, pair.second) }.count()
        }


    //endregion
    //region DropDownField
    private fun createDropDownField(field: Field) =
        (inflate(R.layout.form_dropdown) as TextInputLayout).apply {
            hint = field.hint
            field.tag?.let { tag = it }
            findViewById<MaterialAutoCompleteTextView>(R.id.autocompleteTv).apply {
                coroutineContext.launch {
                    val result = callbacks.requestData(field.tag!!)
                    tag = result
                    this@FormView.progressBar.invisible()
                    withContext(Dispatchers.Main)
                    {
                        val adapter: ArrayAdapter<Pair<String, String>> = ArrayAdapter(
                            context,
                            R.layout.dropdown_item,
                            result
                        )
                        setAdapter(adapter)
                    }

                }


            }
            addLayoutParams(mediumSpace, field.weight)
        }

    //endregion
    //region Validations
    private fun TextInputLayout.attachValidations(
        validationRule: ValidationRule,
        value: String,
        field: Field
    ) {

        val errorMandatory = "${field.name} is mandatory"

        val lengthSatisfied = maxMinLengthSatisfied(validationRule, value, field)
        if (lengthSatisfied != null)
            this.error = lengthSatisfied
        else if (field.inputType != null) {
            when (field.inputType) {
                InputType.NUMBER -> {
                    if (value.isInt()) {
                        val valueSatisfied = maxMinValueSatisfied(validationRule, value, field)
                        if (valueSatisfied != null) {
                            this.error = valueSatisfied
                        } else
                            this.error = null
                    } else
                        this.error = null
                }
                InputType.PHONE -> TODO()
                InputType.TEXT_PASSWORD -> {
                }

            }
        }
        if (validationRule.mandatory) { // this has the highest precedence when the form has been initiated.
            if (value.isEmpty())
                this.error = errorMandatory
            else
                this.error?.let { if (it.toString() == errorMandatory) this.error = null }
            // what is happening here? Well the mandatory check is special ,
            // assume that a validation error has been generated for minMax.
            // in that scenario if this field is mandatory , the else condition would remove the error that minMax has set.
            // So mandatory condition should only remove an error if it is the mandatory error. All other errors should still be displayed.

        }
    }

    private fun maxMinValueSatisfied(
        validationRule: ValidationRule,
        value: String,
        field: Field
    ) =
        when {
            validationRule.maxValue != 0 -> {
                val range = IntRange(validationRule.minValue, validationRule.maxValue)
                if (value.toInt() !in range)
                    "${field.name} should be between  ${validationRule.minValue} and ${validationRule.maxValue}"
                else
                    null
            }
            validationRule.minValue > 0 -> {
                if (value.toInt() < validationRule.minValue)
                    "${field.name} cannot be less than  ${validationRule.minValue}"
                else
                    null
            }
            else -> {
                null
            }
        }

    private fun maxMinLengthSatisfied(
        validationRule: ValidationRule,
        value: String,
        field: Field
    ) =
        when {
            validationRule.maxLength != 0 -> {
                val range = IntRange(validationRule.minLength, validationRule.maxLength)
                if (value.length !in range)
                    "${field.name} should have characters between ${validationRule.minLength} and ${validationRule.maxLength}"
                else
                    null
            }
            validationRule.minLength > 0 -> {
                if (value.length < validationRule.minLength)
                    "${field.name} should have more characters than  ${validationRule.minLength}"
                else
                    null
            }
            else -> {
                null
            }
        }

//endregion


    interface FormCallbacks {
        suspend fun requestData(tag: String): List<Pair<String, String>>
        fun stitchedResult(tag: String)
    }

    //region Extensions
    fun Int.toPx(): Int {
        val metrics = Resources.getSystem().displayMetrics

        return (this * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View =
        LayoutInflater.from(context).inflate(layoutRes, this, false)

    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                afterTextChanged.invoke(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun EditText.validate(message: String, validator: (String) -> Boolean) {

        if (this.parent.parent is TextInputLayout) {
            val parent = this.parent.parent as TextInputLayout
            this.afterTextChanged {
                parent.error = if (validator(it)) null else message
            }
            parent.error = if (validator(this.text.toString())) null else message
        } else {
            this.afterTextChanged {
                this.error = if (validator(it)) null else message
            }
            this.error = if (validator(this.text.toString())) null else message
        }

    }

    fun View.visible() {
        this.visibility = View.VISIBLE
    }

    fun View.invisible() {
        this.visibility = View.INVISIBLE
    }

    //endregion

}