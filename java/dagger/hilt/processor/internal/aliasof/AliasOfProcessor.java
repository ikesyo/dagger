/*
 * Copyright (C) 2020 The Dagger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dagger.hilt.processor.internal.aliasof;

import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.ISOLATING;

import androidx.room.compiler.processing.XAnnotation;
import androidx.room.compiler.processing.XElement;
import androidx.room.compiler.processing.XType;
import androidx.room.compiler.processing.XTypeElement;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dagger.hilt.processor.internal.BaseProcessor;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.ProcessorErrors;
import dagger.internal.codegen.extension.DaggerStreams;
import dagger.internal.codegen.xprocessing.XElements;
import java.util.Set;
import javax.annotation.processing.Processor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;

/** Processes the annotations annotated with {@link dagger.hilt.migration.AliasOf} */
@IncrementalAnnotationProcessor(ISOLATING)
@AutoService(Processor.class)
public final class AliasOfProcessor extends BaseProcessor {
  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return ImmutableSet.of(ClassNames.ALIAS_OF.toString());
  }

  @Override
  public void processEach(XTypeElement annotation, XElement element) throws Exception {
    ProcessorErrors.checkState(
        element.hasAnnotation(ClassNames.SCOPE),
        element,
        "%s should only be used on scopes." + " However, it was found annotating %s",
        annotation,
        XElements.toStableString(element));

    XAnnotation xAnnotation = element.getAnnotation(ClassNames.ALIAS_OF);

    ImmutableList<XTypeElement> defineComponentScopes =
        xAnnotation.getAsTypeList("value").stream()
            .map(XType::getTypeElement)
            .collect(DaggerStreams.toImmutableList());

    ProcessorErrors.checkState(
        defineComponentScopes.size() >= 1,
        element,
        "@AliasOf annotation %s must declare at least one scope to alias.",
        xAnnotation.getClassName());

    new AliasOfPropagatedDataGenerator(XElements.asTypeElement(element), defineComponentScopes)
        .generate();
  }
}
