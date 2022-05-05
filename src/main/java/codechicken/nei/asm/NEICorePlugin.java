package codechicken.nei.asm;

import codechicken.core.launch.CodeChickenCorePlugin;
import codechicken.lib.asm.ASMInit;
import codechicken.nei.Tags;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import java.io.File;
import java.util.Map;

@TransformerExclusions(value = {Tags.GROUPNAME + ".asm"})
public class NEICorePlugin implements IFMLLoadingPlugin, IFMLCallHook
{
    public static File location;

    public NEICorePlugin() {
        ASMInit.init();
    }

    @Override
    public String[] getASMTransformerClass() {
        CodeChickenCorePlugin.versionCheck(CodeChickenCorePlugin.mcVersion, Tags.MODID);
        return new String[]{Tags.GROUPNAME + ".asm.NEITransformer"};
    }

    @Override
    public String getModContainerClass() {
        return Tags.GROUPNAME + ".NEIModContainer";
    }

    @Override
    public String getSetupClass() {
        return Tags.GROUPNAME + ".asm.NEICorePlugin";
    }

    @Override
    public void injectData(Map<String, Object> data) {
        location = (File) data.get("coremodLocation");
        if (location == null)
            location = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public Void call() {
        return null;
    }
}
