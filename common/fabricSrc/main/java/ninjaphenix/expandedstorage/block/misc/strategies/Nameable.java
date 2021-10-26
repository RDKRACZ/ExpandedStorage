/**
 * Copyright 2021 NinjaPhenix
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ninjaphenix.expandedstorage.block.misc.strategies;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

public interface Nameable {
    Text get();

    Text getDefault();

    boolean isCustom();

    void writeName(NbtCompound tag);

    void readName(NbtCompound tag);

    class Mutable implements Nameable {
        private final Text defaultName;
        private Text name;

        public Mutable(Text defaultName) {
            this.defaultName = defaultName;
        }

        @Override
        public Text get() {
            return this.isCustom() ? name : defaultName;
        }

        @Override
        public Text getDefault() {
            return defaultName;
        }

        @Override
        public boolean isCustom() {
            return name != null;
        }

        @Override
        public void writeName(NbtCompound tag) {
            if (name != null) {
                tag.putString("CustomName", Text.Serializer.toJson(name));
            }
        }

        @Override
        public void readName(NbtCompound tag) {
            if (tag.contains("CustomName", NbtElement.STRING_TYPE)) {
                name = Text.Serializer.fromJson(tag.getString("CustomName"));
            }
        }

        public void setName(Text name) {
            this.name = name;
        }
    }

    //class Not implements Nameable {
    //    private final Text name;
    //
    //    public Not(Text name) {
    //        this.name = name;
    //    }
    //
    //    @Override
    //    public Text get() {
    //        return name;
    //    }
    //
    //    @Override
    //    public Text getDefault() {
    //        return name;
    //    }
    //
    //    @Override
    //    public boolean isCustom() {
    //        return false;
    //    }
    //
    //    @Override
    //    public void writeName(NbtCompound tag) {
    //
    //    }
    //
    //    @Override
    //    public void readName(NbtCompound tag) {
    //
    //    }
    //}
}
