package com.cedricziel.idea.typo3.icons;

import com.cedricziel.idea.typo3.util.PhpLangUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class IconReferenceContributor extends PsiReferenceContributor {

    private static final String ICON_FACTORY = "\\TYPO3\\CMS\\Core\\Imaging\\IconFactory";

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        // known method calls
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class).withSuperParent(2, PlatformPatterns.psiElement(MethodReference.class)),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                    StringLiteralExpression stringLiteralExpression = (StringLiteralExpression) element;
                    MethodReference methodReference = (MethodReference) PsiTreeUtil.findFirstParent(stringLiteralExpression, p -> p instanceof MethodReference);

                    if (methodReference == null) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    String methodName = methodReference.getName();
                    if (methodReference.getFirstPsiChild() instanceof Variable) {
                        Variable variable = (Variable) methodReference.getFirstPsiChild();
                        String signature = variable.getSignature();
                        try {
                            Collection<? extends PhpNamedElement> bySignature = PhpIndex.getInstance(element.getProject()).getBySignature(signature);
                            for (PhpNamedElement el : bySignature) {
                                String fqn = el.getFQN();
                                if (fqn.equals(ICON_FACTORY) && methodName.equals("getIcon")) {
                                    return new PsiReference[]{new IconReference(stringLiteralExpression)};
                                }
                            }
                        } catch (RuntimeException e) {
                            // invalid index signature, skip
                        }
                    }

                    String className = PhpLangUtil.getClassName(stringLiteralExpression);
                    if (methodName != null && className != null && methodName.equals("getIcon") && className.equals(ICON_FACTORY)) {
                        return new PsiReference[]{new IconReference(stringLiteralExpression)};
                    }

                    return PsiReference.EMPTY_ARRAY;
                }
            }
        );
    }
}
