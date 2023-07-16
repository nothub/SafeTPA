#!/usr/bin/env sh

set -eu

cd "$(realpath "$(dirname "$(readlink -f "$0")")")"

# TODO: flags
mc_version="1.20.1"
server_dir="./server"
server_port="25565"
spigot_tester_version="1.0.2-Release"
spigot_tester_hash="3de81665bc0e7f098fc6b78d46125ab7e42ee07b534942fc32196ac0556d3863"
plugin_jar="./target/SafeTPA-4.0.0.jar"

paper_api_base="https://api.papermc.io/v2/projects/paper"

if test ! -f "${server_dir}/server.jar"; then

    echo >&2 "downloading paper"
    build_id="$(curl -sSLf  "${paper_api_base}/versions/${mc_version}" | jq -r '.builds|last')"
    file_name="$(curl -sSLf "${paper_api_base}/versions/${mc_version}/builds/${build_id}" |
        jq -r '.downloads.application.name')"
    file_hash="$(curl -sSLf "${paper_api_base}/versions/${mc_version}/builds/${build_id}" |
        jq -r '.downloads.application.sha256')"
    file_url="${paper_api_base}/versions/${mc_version}/builds/${build_id}/downloads/${file_name}"

    mkdir -p "${server_dir}"
    curl -sSLf -o "${server_dir}/server.jar" "${file_url}"
    echo "${file_hash} ${server_dir}/server.jar" | sha256sum -c --status -

    echo >&2 "configuring server"

    {
        echo "motd=test server (${mc_version})"
        echo "server-port=${server_port}"
        echo "spawn-protection=0"
    } >"${server_dir}/server.properties"

    if test -n "${MC_EULA}"; then
        echo "eula=true" >"${server_dir}/eula.txt"
    else
        echo "Do you agree with Mojangs EULA? [Y/n]"
        echo "https://account.mojang.com/documents/minecraft_eula"
        read -n 1 -r
        echo ""
        if test "${REPLY}" = "Y" -o "${REPLY}" = "y"; then
            echo "eula=true" >"${server_dir}/eula.txt"
        fi
    fi

    echo >&2 "preparing plugins"

    mkdir -p "${server_dir}/plugins/bStats"
    echo "enabled: false" >"${server_dir}/plugins/bStats/config.yml"

    curl -sSLf -o "${server_dir}/plugins/SpigotTester.jar" \
        "https://github.com/jwdeveloper/SpigotTester/releases/download/${spigot_tester_version}/SpigotTester.jar"
    echo "${spigot_tester_hash} ${server_dir}/plugins/SpigotTester.jar" | sha256sum -c --status -

fi

mkdir -p "${server_dir}/plugins"
cp "${plugin_jar}" "${server_dir}/plugins/"
