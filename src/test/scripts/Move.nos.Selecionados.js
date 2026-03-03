/*
Test script provided by ericodb, https://github.com/ericodb.
See issue https://github.com/Gubaer/josm-scripting-plugin/issues/115

 */
"use strict";

const MainApplication = Java.type("org.openstreetmap.josm.gui.MainApplication");
const Notification    = Java.type("org.openstreetmap.josm.gui.Notification");
const MoveCommand     = Java.type("org.openstreetmap.josm.command.MoveCommand");
const SequenceCommand = Java.type("org.openstreetmap.josm.command.SequenceCommand");
const UndoRedoHandler = Java.type("org.openstreetmap.josm.data.UndoRedoHandler");
const UIManager       = Java.type("javax.swing.UIManager");
const JDialog         = Java.type("javax.swing.JDialog");
const JPanel          = Java.type("javax.swing.JPanel");
const JLabel          = Java.type("javax.swing.JLabel");
const JButton         = Java.type("javax.swing.JButton");
const JSpinner        = Java.type("javax.swing.JSpinner");
const SpinnerNumberModel = Java.type("javax.swing.SpinnerNumberModel");
const BoxLayout       = Java.type("javax.swing.BoxLayout");
const BorderFactory   = Java.type("javax.swing.BorderFactory");
const BorderLayout    = Java.type("java.awt.BorderLayout");
const Dimension       = Java.type("java.awt.Dimension");
const Font            = Java.type("java.awt.Font");
const ArrayList       = Java.type("java.util.ArrayList");

(function() {
    const layer = MainApplication.getLayerManager().getEditLayer();
    if (!layer) {
        new Notification("Nenhuma camada ativa.")
            .setIcon(UIManager.getIcon("OptionPane.errorIcon"))
            .show();
        return;
    }

    const initialPositions = new Map();
    let hasMoved = false;

    // --- LÓGICA DE MOVIMENTO ---
    const aplicarMovimento = function(distancia, tipo) {
        try {
            const currentLayer = MainApplication.getLayerManager().getEditLayer();
            if (!currentLayer) return;
            const nodes = currentLayer.data.getSelectedNodes();
            
            if (nodes.size() < 2) {
                new Notification("Selecione pelo menos 2 nós.")
                    .setIcon(UIManager.getIcon("OptionPane.warningIcon"))
                    .show();
                return;
            }

            const it = nodes.iterator();
            const n1 = it.next();
            const n2 = it.next();
            const c1 = n1.getCoor();
            const c2 = n2.getCoor();
            
            const latRad = ((c1.lat() + c2.lat()) / 2.0) * (Math.PI / 180.0);
            const mPerDegLat = 111319.492;
            const mPerDegLon = mPerDegLat * Math.cos(latRad);
            
            const dxM = (c2.lon() - c1.lon()) * mPerDegLon;
            const dyM = (c2.lat() - c1.lat()) * mPerDegLat;
            const comp = Math.sqrt(dxM * dxM + dyM * dyM);
            if (comp < 1e-6) return;

            let ux = dxM / comp;
            let uy = dyM / comp;
            if (tipo === "dy") { const tmp = ux; ux = -uy; uy = tmp; }

            const scale = 1.0 / Math.cos(c1.lat() * Math.PI / 180.0);
            const dEnX = ux * distancia * scale;
            const dEnY = uy * distancia * scale;

            const cmds = new ArrayList();
            const itAll = nodes.iterator();
            while (itAll.hasNext()) {
                const n = itAll.next();
                if (!initialPositions.has(n)) initialPositions.set(n, n.getEastNorth());
                cmds.add(new MoveCommand(n, dEnX, dEnY));
            }
            UndoRedoHandler.getInstance().add(new SequenceCommand("Ajuste Fino", cmds));
            hasMoved = true;
        } catch (e) {
            print("Erro na execução: " + e);
        }
    };

    // --- INTERFACE (MODAL) ---
    const dialog = new JDialog(MainApplication.getMainFrame(), "Ajuste Fino", true);
    
    const mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    const model = new SpinnerNumberModel(1.0, 0.0, 50.0, 0.5);
    const spinner = new JSpinner(model);
    spinner.setPreferredSize(new Dimension(85, 28));
    const sP = new JPanel(); 
    sP.add(new JLabel("Passo (m):")); 
    sP.add(spinner);
    mainPanel.add(sP);

    const btnSize = new Dimension(65, 30);
    const btnFont = new Font("SansSerif", Font.BOLD, 14);

    const criarGrupo = (tit, tp) => {
        const p = new JPanel(); 
        p.setBorder(BorderFactory.createTitledBorder(tit));
        const bM = new JButton("-"); 
        const bP = new JButton("+");
        [bM, bP].forEach(b => { 
            b.setPreferredSize(btnSize); 
            b.setFont(btnFont); 
            p.add(b);
            b.addActionListener(function() { 
                aplicarMovimento((b.getText() === "+" ? 1 : -1) * model.getValue(), tp); 
            });
        });
        return p;
    };

    mainPanel.add(criarGrupo("Paralelo", "dx"));
    mainPanel.add(criarGrupo("Perpendicular", "dy"));

    // --- RODAPÉ COM BOTÕES E ÍCONES ---
    const footer = new JPanel();
    const btnOk = new JButton("Concluir", UIManager.getIcon("OptionPane.okIcon"));
    const btnCc = new JButton("Reverter", UIManager.getIcon("OptionPane.noIcon"));

    btnOk.setFont(new Font("SansSerif", Font.PLAIN, 12));
    btnCc.setFont(new Font("SansSerif", Font.PLAIN, 12));

    btnOk.addActionListener(function() {
        if (hasMoved) {
            new Notification("Ajustes finalizados.")
                .setIcon(UIManager.getIcon("OptionPane.informationIcon"))
                .show();
        }
        dialog.dispose();
    });

    btnCc.addActionListener(function() {
        if (hasMoved) {
            const cmds = new ArrayList();
            initialPositions.forEach((pos, node) => {
                const cur = node.getEastNorth();
                cmds.add(new MoveCommand(node, pos.east() - cur.east(), pos.north() - cur.north()));
            });
            UndoRedoHandler.getInstance().add(new SequenceCommand("Reverter Ajustes", cmds));
            new Notification("Ajustes revertidos.")
                .setIcon(UIManager.getIcon("OptionPane.warningIcon"))
                .show();
        }
        dialog.dispose();
    });

    footer.add(btnOk); 
    footer.add(btnCc);
    
    const content = new JPanel(new BorderLayout());
    content.add(mainPanel, BorderLayout.CENTER);
    content.add(footer, BorderLayout.SOUTH);

    dialog.setContentPane(content);
    dialog.pack();
    dialog.setAlwaysOnTop(false);
    dialog.setLocationRelativeTo(MainApplication.getMainFrame());

    // --- EXECUÇÃO ---
    dialog.setVisible(true); 
})();
