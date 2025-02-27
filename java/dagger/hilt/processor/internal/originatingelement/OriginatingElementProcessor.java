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

package dagger.hilt.processor.internal.originatingelement;

import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.ISOLATING;

import androidx.room.compiler.processing.XElement;
import androidx.room.compiler.processing.XElementKt;
import androidx.room.compiler.processing.XTypeElement;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import dagger.hilt.processor.internal.BaseProcessor;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.ProcessorErrors;
import dagger.hilt.processor.internal.Processors;
import dagger.internal.codegen.xprocessing.XElements;
import javax.annotation.processing.Processor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;

/**
 * Processes the annotations annotated with {@link dagger.hilt.codegen.OriginatingElement} to check
 * that they're only used on top-level classes and the value passed is also a top-level class.
 */
@IncrementalAnnotationProcessor(ISOLATING)
@AutoService(Processor.class)
public final class OriginatingElementProcessor extends BaseProcessor {

  @Override
  public ImmutableSet<String> getSupportedAnnotationTypes() {
    return ImmutableSet.of(ClassNames.ORIGINATING_ELEMENT.toString());
  }

  @Override
  public void processEach(XTypeElement annotation, XElement element) {
    ProcessorErrors.checkState(
        XElementKt.isTypeElement(element) && Processors.isTopLevel(element),
        element,
        "@%s should only be used to annotate top-level types, but found: %s",
        annotation.getClassName().simpleName(),
        XElements.toStableString(element));

    XTypeElement topLevelClassElement =
        element.getAnnotation(ClassNames.ORIGINATING_ELEMENT)
            .getAsType("topLevelClass")
            .getTypeElement();

    // TODO(bcorso): ProcessorErrors should allow us to point to the annotation too.
    ProcessorErrors.checkState(
        Processors.isTopLevel(topLevelClassElement),
        element,
        "@%s.topLevelClass value should be a top-level class, but found: %s",
        annotation.getClassName().simpleName(),
        topLevelClassElement.getClassName());
  }
}
