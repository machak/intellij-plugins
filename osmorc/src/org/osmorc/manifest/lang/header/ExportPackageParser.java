/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.manifest.lang.header;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.ContainerUtilRt;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.ManifestConstants;
import org.jetbrains.lang.manifest.psi.ManifestTokenType;
import org.jetbrains.lang.manifest.header.HeaderParser;
import org.osmorc.manifest.lang.psi.Attribute;
import org.osmorc.manifest.lang.psi.Clause;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;
import org.jetbrains.lang.manifest.psi.ManifestToken;

import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ExportPackageParser extends BasePackageParser {
  public static final HeaderParser INSTANCE = new ExportPackageParser();

  private static boolean isPackageRef(final PsiElement element) {
    final boolean result;
    if (element instanceof ManifestToken) {
      ManifestToken manifestToken = (ManifestToken)element;
      result = manifestToken.getTokenType() != ManifestTokenType.SEMICOLON;
    }
    else {
      result = true;
    }

    return result;
  }

  @NotNull
  @Override
  public PsiReference[] getReferences(@NotNull HeaderValuePart headerValuePart) {
    if (headerValuePart.getParent() instanceof Clause) {
      final PsiElement element = headerValuePart.getOriginalElement();
      if (isPackageRef(element.getPrevSibling())) {
        return getPackageReferences(headerValuePart);
      }
    }
    else if (headerValuePart.getParent() instanceof Attribute) {
      final Attribute attribute = (Attribute)headerValuePart.getParent();
      if (ManifestConstants.Attributes.USES.equals(attribute.getName()) &&
          !ManifestConstants.Attributes.USES.equals(headerValuePart.getUnwrappedText())) {
        List<PsiReference> references = ContainerUtil.newArrayList();
        for (ASTNode astNode : headerValuePart.getNode().getChildren(TokenSet.create(ManifestTokenType.HEADER_VALUE_PART))) {
          if (astNode instanceof ManifestToken) {
            ManifestToken manifestToken = (ManifestToken)astNode;
            ContainerUtil.addAll(references, getPackageReferences(manifestToken));
          }
        }
        return ContainerUtilRt.toArray(references, new PsiReference[references.size()]);
      }
    }
    return PsiReference.EMPTY_ARRAY;
  }
}
