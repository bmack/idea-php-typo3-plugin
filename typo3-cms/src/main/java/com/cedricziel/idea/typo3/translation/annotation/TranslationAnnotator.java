package com.cedricziel.idea.typo3.translation.annotation;

import com.cedricziel.idea.typo3.index.ResourcePathIndex;
import com.cedricziel.idea.typo3.util.TranslationUtil;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ConcatenationExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class TranslationAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {

        if (!(psiElement instanceof StringLiteralExpression) && !(psiElement.getParent() instanceof StringLiteralExpression)) {
            return;
        }

        StringLiteralExpression literalExpression;
        if (psiElement instanceof StringLiteralExpression) {
            literalExpression = (StringLiteralExpression) psiElement;
        } else {
            literalExpression = (StringLiteralExpression) psiElement.getParent();
        }

        String value = literalExpression.getContents();
        if (TranslationUtil.isTranslationKeyString(value) && value.length() > 4) {
            annotateTranslationUsage(psiElement, annotationHolder, literalExpression, value);
        }
    }

    private void annotateTranslationUsage(PsiElement psiElement, AnnotationHolder annotationHolder, StringLiteralExpression literalExpression, String value) {
        if (value.endsWith(":") || literalExpression.getParent() instanceof ConcatenationExpression) {
            return;
        }

        annotateTranslation(psiElement, annotationHolder, value);
    }

    private void annotateTranslation(PsiElement psiElement, AnnotationHolder annotationHolder, String value) {
        if (TranslationUtil.keyExists(psiElement.getProject(), value)) {
            annotationHolder.createInfoAnnotation(psiElement, null);
        } else {
            if (ResourcePathIndex.projectContainsResourceFile(psiElement.getProject(), value)) {
                annotationHolder.createInfoAnnotation(psiElement, "Translation file reference");

                return;
            }

            annotationHolder.createWeakWarningAnnotation(psiElement, "Unresolved translation - this may occur if you defined the translation key only in TypoScript");
        }
    }
}
