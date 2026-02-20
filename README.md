ZstdMC是一个旨在优化多人模式网络占用的mod



本mod使用zstandard标准库替换mc内置的压缩解压缩模块，从而提高压缩比节省流量

绝大多数情况下，不会对游戏产生负面影响

多人游戏可以节省约40~90%的网络带宽，且不会占用太多CPU资源

您只需下载mod文件，放入服务端和客户端文件夹，然后更改服务端配置 network-compression-threshold 大于 128 即可（推荐256）

注:多数情况您应选择fat版mod文件，如果遇到崩溃情况，请更换为fit版本即可解决

===================================================================


ZstdMC: Multiplayer Network Optimization
ZstdMC is a performance-focused mod designed to optimize network bandwidth usage in multiplayer environments.

By replacing Minecraft's built-in compression and decompression modules with the Zstandard (zstd) library, this mod achieves significantly higher compression ratios, resulting in substantial data savings. In most scenarios, it provides these benefits with no negative impact on the game's stability or performance.

Key Features
Bandwidth Efficiency: Saves approximately 40% to 90% of network traffic in multiplayer mode.

Low Overhead: High-performance compression that minimizes CPU resource consumption.

Easy Setup: Simply drop the mod file into both the server and client mods folders.

Configuration Requirement
To enable the optimization, ensure your server's server.properties file has the following parameter set:

network-compression-threshold=128 (or higher)

Important Note on Versions
Fat Version (Recommended): In most cases, you should use the Fat version, which includes all necessary libraries.

Fit Version: If you experience crashes or compatibility issues, please switch to the Fit version to resolve them.
