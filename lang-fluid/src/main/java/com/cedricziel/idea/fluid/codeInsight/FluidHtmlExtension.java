package com.cedricziel.idea.fluid.codeInsight;

import com.cedricziel.idea.fluid.index.FluidIndexUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.SchemaPrefix;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.HtmlXmlExtension;
import org.jetbrains.annotations.Nullable;

/*
 * Is this really necessary? We usually dont have prefixed attributes.
 */
public class FluidHtmlExtension extends HtmlXmlExtension {
    @Nullable
    private static SchemaPrefix findAttributeSchema(XmlTag context, String namespacePrefix, int offset) {
        for (XmlAttribute attribute: context.getAttributes()) {
            if (attribute.getName().startsWith(namespacePrefix)) {
                return new SchemaPrefix(attribute, TextRange.create(offset, namespacePrefix.length()), namespacePrefix.substring(offset));
            }
        }
        return null;
    }

    @Override
    public boolean isAvailable(PsiFile file) {
        return super.isAvailable(file) && FluidIndexUtil.hasFluid(file.getProject());
    }

    @Override
    public boolean isRequiredAttributeImplicitlyPresent(XmlTag tag, String attrName) {
        /*
        Maybe useful to overcome implicit attributes / exchange
        for (XmlAttribute attribute: tag.getAttributes()) {
            if (("ng-" + attrName).equals(DirectiveUtil.normalizeAttributeName(attribute.getName()))) {
                return true;
            }
            if (("[" + attrName + "]").equals(attribute.getName())) {
                return true;
            }
        }
        */

        return super.isRequiredAttributeImplicitlyPresent(tag, attrName);
    }

    @Override
    public SchemaPrefix getPrefixDeclaration(XmlTag context, String namespacePrefix) {
        if ("f".equals(namespacePrefix)) {
            SchemaPrefix attribute = findAttributeSchema(context, namespacePrefix, 0);
            if (attribute != null) return attribute;
        }

        /*
        TODO: Implement additional schemas
        if (namespacePrefix != null && (namespacePrefix.startsWith("(") || namespacePrefix.startsWith("["))) {
            SchemaPrefix attribute = findAttributeSchema(context, namespacePrefix, 1);
            if (attribute != null) return attribute;
        }
        */
        return super.getPrefixDeclaration(context, namespacePrefix);
    }
}
