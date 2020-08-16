package com.seed.widgets.formview

import com.google.gson.annotations.SerializedName

data class Form(val title: String?, val padding: Int?, val rows: List<Row>)
data class Row(val fields: List<Field>)
data class Field(
    val type: Type,
    val text: String?,
    val name: String,
    val dropDownSelectedId: String?=null,
    val dropDownSelectedValue: String?=null,
    val tag: String?,
    val hint: String,
    val weight: Int?,
    val layoutParam: LayoutParam?,
    val inputType: InputType?,
    val validationRule: ValidationRule?
)
data class LayoutParam(val gravity: Gravity?, val width: Width?, val margins: String?)
data class ValidationRule(val mandatory:Boolean,val minValue: Int, val maxValue: Int,val maxLength:Int,val minLength:Int)

enum class Type {
    @SerializedName("TextField")
    TEXTFIELD,
    @SerializedName("ButtonField")
    BUTTON,
    @SerializedName("DropDown")
    DROPDOWN,
    @SerializedName("SubmitButtonField")
    SUBMIT_BUTTON_FIELD,
}

enum class Width {
    @SerializedName("WrapContent")
    WRAP_CONTENT,
}

enum class Gravity(val value: Int) {
    @SerializedName("Left")
    LEFT(3),
    @SerializedName("Right")
    RIGHT(5),
}
enum class InputType {
    @SerializedName("Number")
    NUMBER,
    @SerializedName("Phone")
    PHONE,
    @SerializedName("TextPassword")
    TEXT_PASSWORD
}



//data class      Pair<A,B>(out val  first: A, val out second:B):
 data class Pair<out A, out B>(
     val first: A,
     val second: B
) {

    /**
     * Returns string representation of the [Pair] including its [first] and [second] values.
     */
    public override fun toString(): String = "$second"
}



