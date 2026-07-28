package digital.guimauve.pkg.presentation.freemarker

import digital.guimauve.pkg.domain.services.TranslateService
import freemarker.core.Environment
import freemarker.template.*

/**
 * The `<@t key="..." />` directive of the templates. The language comes from the `locale` variable
 * every response puts in its model.
 */
class TranslateDirective(
    private val translateService: TranslateService,
) : TemplateDirectiveModel {

    override fun execute(
        env: Environment?,
        params: MutableMap<Any?, Any?>?,
        loopVars: Array<out TemplateModel>?,
        body: TemplateDirectiveBody?,
    ) {
        val key = params?.get("key") as? TemplateScalarModel
            ?: throw TemplateModelException("The `t` directive requires a `key` parameter")
        val language = (env?.getVariable("locale") as? TemplateScalarModel)?.asString ?: "en"
        env?.out?.write(translateService.translate(key.asString, language))
    }

}
