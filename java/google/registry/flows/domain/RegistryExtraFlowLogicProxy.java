// Copyright 2016 The Nomulus Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package google.registry.flows.domain;

import com.google.common.base.Optional;
import google.registry.flows.EppException;
import google.registry.flows.EppException.CommandFailedException;
import google.registry.model.domain.DomainBase;
import google.registry.model.registry.Registry;
import java.util.HashMap;
import javax.annotation.Nullable;

/**
 * Static class to return the correct {@link RegistryExtraFlowLogic} for a particular TLD.
 * Eventually, this will probably be replaced with dependency injection, barring unforeseen
 * complications.
 */
public class RegistryExtraFlowLogicProxy {

  private static final HashMap<String, Class<? extends RegistryExtraFlowLogic>>
      extraLogicOverrideMap = new HashMap<>();

  public static void setOverride(
      String tld, Class<? extends RegistryExtraFlowLogic> extraLogicClass) {
    if (Registry.get(tld) == null) {
      throw new IllegalArgumentException(tld + " does not exist");
    }
    extraLogicOverrideMap.put(tld, extraLogicClass);
  }

  public static <D extends DomainBase> Optional<RegistryExtraFlowLogic>
      newInstanceForDomain(@Nullable D domain) throws EppException {
    if (domain == null) {
      return Optional.absent();
    } else {
      return newInstanceForTld(domain.getTld());
    }
  }

  public static Optional<RegistryExtraFlowLogic>
      newInstanceForTld(String tld) throws EppException {
    if (extraLogicOverrideMap.containsKey(tld)) {
      try {
        return Optional.<RegistryExtraFlowLogic>of(
            extraLogicOverrideMap.get(tld).getConstructor().newInstance());
      } catch (ReflectiveOperationException ex) {
        throw new CommandFailedException();
      }
    }
    return Optional.absent();
  }
}
