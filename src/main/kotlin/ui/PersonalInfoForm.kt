//package ui
//
//import com.jgoodies.forms.builder.DefaultFormBuilder
//import com.jgoodies.forms.builder.FormBuilder
//import com.jgoodies.forms.layout.CellConstraints
//import com.jgoodies.forms.layout.FormLayout
//import java.awt.TextField
//import javax.swing.*
//
//
//class PersonalInfoForm {
//    private var layout: FormLayout = FormLayout(
//        "right:pref, 4dlu, 50dlu, 4dlu,min",
//        "pref,4dlu,pref,4dlu,pref,2dlu,pref,2dlu,pref,2dlu,pref,2dlu,pref,4dlu,pref,2dlu,pref,2dlu,pref,2dlu,pref,2dlu,pref,4dlu,pref" )
//    private var panel: JPanel = JPanel(layout)
//    private val idTextField = TextField()
//    private val searchButton = JButton("Найти")
//    private val ageTextField = TextField()
//    private val saveButton = JButton("Сохранить")
//    private val builder = FormBuilder.create()
//
//
//
//    init {
//        val cc = CellConstraints()
//
//
//
//
//        builder.add("Имя", idTextField)
//        builder.add(searchButton)
//        builder
//        builder.nextLine()
//        builder.add("Возраст", ageTextField)
//        builder.nextLine()
//        builder.nextLine()
//
//        val jRadioButtonTexts = listOf("меньше 1 года", "от 1 до 2 лет", "от 2 до 3 лет", "больше 3 лет")
//
//
//        builder.append("Опыт программирования")
//        builder.nextLine()
//        builder.nextLine()
//
//        val programExperienceButtons = createJRadioButtons(jRadioButtonTexts)
//        for (programExperienceButton in programExperienceButtons) {
//            builder.nextColumn(2)
//            builder.append(programExperienceButton)
//            builder.nextLine()
//            builder.nextLine()
//        }
//
//        builder.append("Опыт программирования на этом языке")
//        builder.nextLine()
//        builder.nextLine()
//
//        val languageExperienceButtons = createJRadioButtons(jRadioButtonTexts)
//        for (languageExperienceButton in languageExperienceButtons) {
//            builder.nextColumn(2)
//            builder.append(languageExperienceButton)
//            builder.nextLine()
//            builder.nextLine()
//        }
//
//        builder.append(saveButton)
//
//
//
//        panel.add(JLabel("Имя"), cc.xy(2, 1))
//        panel.add(idTextField, cc.xy(4, 1))
//        panel.add(searchButton, cc.xy(6, 1))
//
//        panel.add(JLabel("Возраст"), cc.xy (2, 3))
//        panel.add(ageTextField, cc.xy (4, 3))
//
//
//        val jRadioButtonTexts = listOf("меньше 1 года", "от 1 до 2 лет", "от 2 до 3 лет", "больше 3 лет")
//
//        val programExperienceButtons = createJRadioButtons(jRadioButtonTexts)
//        panel.add(JLabel("Опыт программирования"), cc.xyw(2, 5, 5))
//        panel.add(programExperienceButtons[0], cc.xyw(4, 7, 3))
//        panel.add(programExperienceButtons[1], cc.xyw(4, 9, 3))
//        panel.add(programExperienceButtons[2], cc.xyw(4, 11, 3))
//        panel.add(programExperienceButtons[3], cc.xyw(4, 13, 3))
//
//
//        val languageExperienceButtons = createJRadioButtons(jRadioButtonTexts)
//        panel.add(JLabel("Опыт программирования на этом языке"), cc.xyw(2, 15, 5))
//        panel.add(languageExperienceButtons[0], cc.xyw(4, 17, 3))
//        panel.add(languageExperienceButtons[1], cc.xyw(4, 19, 3))
//        panel.add(languageExperienceButtons[2], cc.xyw(4, 21, 3))
//        panel.add(languageExperienceButtons[3], cc.xyw(4, 23, 3))
//
//        panel.add(saveButton, cc.xy(4, 25))
//
//    }
//
//    private fun createJRadioButtons(texts: List<String>) : List<JRadioButton> {
//        val buttonGroup = ButtonGroup()
//        val buttons = texts.map { JRadioButton(it) }
//        buttons.forEach { buttonGroup.add(it) }
//        return buttons
//    }
//
//    fun content() : JComponent = builder.panel
//
//}