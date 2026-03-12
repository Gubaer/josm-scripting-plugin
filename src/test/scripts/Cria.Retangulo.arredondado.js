"use strict";

// IMPORTS
const MainApplication    = Java.type("org.openstreetmap.josm.gui.MainApplication");
const Notification       = Java.type("org.openstreetmap.josm.gui.Notification");
const Node               = Java.type("org.openstreetmap.josm.data.osm.Node");
const Way                = Java.type("org.openstreetmap.josm.data.osm.Way");
const AddCommand         = Java.type("org.openstreetmap.josm.command.AddCommand");
const SequenceCommand    = Java.type("org.openstreetmap.josm.command.SequenceCommand");
const ProjectionRegistry = Java.type("org.openstreetmap.josm.data.projection.ProjectionRegistry");
const EastNorth          = Java.type("org.openstreetmap.josm.data.coor.EastNorth");
const UndoRedoHandler    = Java.type("org.openstreetmap.josm.data.UndoRedoHandler");

const JDialog         = Java.type("javax.swing.JDialog");
const JPanel          = Java.type("javax.swing.JPanel");
const JLabel          = Java.type("javax.swing.JLabel");
const JButton         = Java.type("javax.swing.JButton");
const BorderFactory   = Java.type("javax.swing.BorderFactory");
const BoxLayout       = Java.type("javax.swing.BoxLayout");
const Box             = Java.type("javax.swing.Box");
const UIManager       = Java.type("javax.swing.UIManager");
const SwingUtilities  = Java.type("javax.swing.SwingUtilities");
const GridBagLayout      = Java.type("java.awt.GridBagLayout");
const GridBagConstraints = Java.type("java.awt.GridBagConstraints");
const Insets             = Java.type("java.awt.Insets");
const Dimension          = Java.type("java.awt.Dimension");
const FlowLayout         = Java.type("java.awt.FlowLayout");
const Font               = Java.type("java.awt.Font");
const ArrayList          = Java.type("java.util.ArrayList");

const ActionListener      = Java.extend(Java.type("java.awt.event.ActionListener"));
const WindowAdapter       = Java.extend(Java.type("java.awt.event.WindowAdapter"));
const LayerChangeListener = Java.extend(
    Java.type("org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener")
);

// VERIFICAÇÃO INICIAL
const layer = MainApplication.getLayerManager().getEditLayer();
if (!layer || !layer.data) {
    new Notification("Nenhuma camada de edição ativa.")
        .setIcon(UIManager.getIcon("OptionPane.errorIcon")).show();
} else {
    const sel = layer.data.getSelectedWays();
    if (sel.isEmpty()) {
        new Notification("Selecione um polígono fechado.")
            .setIcon(UIManager.getIcon("OptionPane.warningIcon")).show();
    } else {
        const poly = sel.iterator().next();
        if (!poly.isClosed()) {
            new Notification("O polígono selecionado não é fechado.")
                .setIcon(UIManager.getIcon("OptionPane.warningIcon")).show();
        } else {
            SwingUtilities.invokeLater(function() { mostrarDialogo(poly); });
        }
    }
}

// GEOMETRIA

// Área com sinal via fórmula de Shoelace — positivo = CCW, negativo = CW
function signed_area(pts) {
    let a = 0;
    const n = pts.length;
    for (let i = 0; i < n; i++) {
        const j = (i + 1) % n;
        a += pts[i].east() * pts[j].north();
        a -= pts[j].east() * pts[i].north();
    }
    return a / 2;
}

// Produto cruzado 2D: (prev→curr) × (curr→next)
// CCW polígono: cross < 0 → convexo (canto externo), cross > 0 → côncavo (canto interno)
// CW  polígono: invertido
function cross2d(prev, curr, next) {
    const ax = curr.east()  - prev.east(),  ay = curr.north() - prev.north();
    const bx = next.east()  - curr.east(),  by = next.north() - curr.north();
    return ax * by - ay * bx;
}

// Ângulo entre dois vetores em radianos (sempre positivo, 0..PI)
function angle_between(prev, curr, next) {
    const ax = prev.east() - curr.east(), ay = prev.north() - curr.north();
    const bx = next.east() - curr.east(), by = next.north() - curr.north();
    const dot = ax * bx + ay * by;
    const mag = Math.hypot(ax, ay) * Math.hypot(bx, by);
    if (mag < 1e-12) return Math.PI;
    return Math.acos(Math.max(-1, Math.min(1, dot / mag)));
}

// Normaliza vetor
function norm(ax, ay) {
    const m = Math.hypot(ax, ay);
    return m < 1e-12 ? [0, 0] : [ax / m, ay / m];
}

// Gera os pontos do arco de arredondamento de um vértice
// convex=true → canto externo (arco para fora), false → canto interno (arco para dentro)
function arco_vertice(prev, curr, next, radius, segments, convex) {
    // Vetores das arestas chegando no vértice
    const [ax, ay] = norm(prev.east() - curr.east(), prev.north() - curr.north());
    const [bx, by] = norm(next.east() - curr.east(), next.north() - curr.north());

    const half_angle = angle_between(prev, curr, next) / 2;
    if (half_angle < 1e-6 || half_angle > Math.PI - 1e-6) return [curr]; // vértice reto

    // Distância ao longo da aresta onde o arco começa
    const tang_dist = radius / Math.tan(half_angle);

    // Pontos de tangência nas duas arestas
    const t1x = curr.east()  + ax * tang_dist;
    const t1y = curr.north() + ay * tang_dist;
    const t2x = curr.east()  + bx * tang_dist;
    const t2y = curr.north() + by * tang_dist;

    // Centro do arco: a partir do ponto de tangência, perpendicular à aresta
    // Para canto convexo o centro fica "dentro" do polígono
    // A perpendicular correta é obtida girando o vetor da aresta 90°
    // O sinal depende de convex
    const sign = convex ? 1 : -1;
    const cx = t1x + (-ay) * radius * sign;
    const cy = t1y + ( ax) * radius * sign;

    // Ângulos de início e fim do arco em torno do centro
    const ang1 = Math.atan2(t1y - cy, t1x - cx);
    const ang2 = Math.atan2(t2y - cy, t2x - cx);

    // Escolhe direção do arco para que fique do lado certo
    let delta = ang2 - ang1;
    if (convex) {
        // Canto externo: arco deve girar no sentido que "encolhe" o canto
        if (delta > 0) delta -= 2 * Math.PI;
    } else {
        // Canto interno: arco vai no outro sentido
        if (delta < 0) delta += 2 * Math.PI;
    }

    const pts = [];
    for (let i = 0; i <= segments; i++) {
        const t = i / segments;
        const a = ang1 + delta * t;
        pts.push(new EastNorth(cx + Math.cos(a) * radius, cy + Math.sin(a) * radius));
    }
    return pts;
}

// ESTADO GLOBAL DO DIÁLOGO
function mostrarDialogo(poly) {
    const ds   = layer.data;
    const proj = ProjectionRegistry.getProjection();

    // Extrai pontos do polígono em coordenadas de projeção (sem o nó de fechamento)
    const orig_pts = [];
    for (let i = 0; i < poly.getNodesCount() - 1; i++) {
        orig_pts.push(proj.latlon2eastNorth(poly.getNode(i).getCoor()));
    }

    // Orientação do polígono
    const area_sign = signed_area(orig_pts); // >0 CCW, <0 CW
    const is_ccw    = area_sign > 0;

    // Filtra nós intermediários (ângulo ≈ 180°, cross ≈ 0) — não são vértices reais
    const STRAIGHT_THRESHOLD = 1e-6; // cross normalizado abaixo disso = nó em linha reta
    const raw_n = orig_pts.length;
    const pts_filtrados = orig_pts.filter(function(_, i) {
        const prev = orig_pts[(i - 1 + raw_n) % raw_n];
        const curr = orig_pts[i];
        const next = orig_pts[(i + 1) % raw_n];
        const d1 = Math.hypot(curr.east()-prev.east(), curr.north()-prev.north());
        const d2 = Math.hypot(next.east()-curr.east(), next.north()-curr.north());
        const norm_cross = Math.abs(cross2d(prev, curr, next)) / Math.max(d1 * d2, 1e-12);
        return norm_cross > STRAIGHT_THRESHOLD;
    });

    const n = pts_filtrados.length;

    // Classifica convexidade usando cross2d + orientação do array atual.
    // Normaliza para CCW antes: assim cross < 0 = convexo é sempre correto.
    // A normalização é feita apenas sobre a cópia de trabalho, nunca no mapa.
    function calcular_convex(pts) {
        const cn   = pts.length;
        const area = signed_area(pts);
        // Se CW (area < 0), inverte para calcular e depois inverte o resultado de volta
        const ccw  = area >= 0 ? pts : pts.slice().reverse();
        const conv = ccw.map(function(_, i) {
            const prev = ccw[(i - 1 + cn) % cn];
            const curr = ccw[i];
            const next = ccw[(i + 1) % cn];
            return cross2d(prev, curr, next) < 0; // CCW: cross < 0 = convexo
        });
        // Se invertemos o array para calcular, a ordem dos resultados está invertida
        return area >= 0 ? conv : conv.reverse();
    }

    // Conjunto de vértices reais — mutável para permitir inversão de orientação
    let orig_pts_filt = pts_filtrados;
    let vertex_convex_mut = calcular_convex(orig_pts_filt);

    // Recalcula vertex_convex a partir do array atual (usado após inversão)
    function recalcular_convex() {
        vertex_convex_mut = calcular_convex(orig_pts_filt);
    }

    // Comprimento mínimo das arestas adjacentes a cada vértice
    const max_radius_per_vertex = orig_pts_filt.map(function(_, i) {
        const prev = orig_pts_filt[(i - 1 + n) % n];
        const curr = orig_pts_filt[i];
        const next = orig_pts_filt[(i + 1) % n];
        const d1   = Math.hypot(curr.east() - prev.east(), curr.north() - prev.north());
        const d2   = Math.hypot(next.east() - curr.east(), next.north() - curr.north());
        const half  = angle_between(prev, curr, next) / 2;
        if (half < 0.01) return Infinity;
        return Math.tan(half) * Math.min(d1, d2) / 2;
    });
    const max_radius = Math.min(...max_radius_per_vertex.filter(v => isFinite(v)));

    // Dimensão inicial do raio: 20% do menor segmento dos vértices reais
    const min_seg = Math.min(...orig_pts_filt.map(function(p, i) {
        const next = orig_pts_filt[(i + 1) % n];
        return Math.hypot(next.east() - p.east(), next.north() - p.north());
    }));
    const raio_inicial = Math.min(min_seg * 0.2, max_radius * 0.5);

    let state = { radius: raio_inicial, segments: 4, previewCriado: false };

    // Gera os pontos do polígono arredondado
    function gerar_pontos_arredondados() {
        const r = Math.min(state.radius, max_radius * 0.99);
        const s = state.segments;
        const resultado = [];

        for (let i = 0; i < n; i++) {
            const prev = orig_pts_filt[(i - 1 + n) % n];
            const curr = orig_pts_filt[i];
            const next = orig_pts_filt[(i + 1) % n];
            const convex = vertex_convex_mut[i];

            const arco = arco_vertice(prev, curr, next, r, s, convex);
            arco.forEach(function(p) { resultado.push(p); });
        }
        return resultado;
    }

    // Preview
    function desenhar_preview() {
        if (state.previewCriado) {
            UndoRedoHandler.getInstance().undo();
            state.previewCriado = false;
        }

        const pts  = gerar_pontos_arredondados();
        const cmds = new ArrayList();
        const nodes_list = new ArrayList();

        pts.forEach(function(p) {
            const nd = new Node(proj.eastNorth2latlon(p));
            cmds.add(new AddCommand(ds, nd));
            nodes_list.add(nd);
        });
        nodes_list.add(nodes_list.get(0)); // fecha

        const way = new Way();
        way.setNodes(nodes_list);
        cmds.add(new AddCommand(ds, way));

        UndoRedoHandler.getInstance().add(new SequenceCommand("Preview Polígono Arredondado", cmds));
        state.previewCriado = true;
        layer.invalidate();
        MainApplication.getMap().mapView.repaint();
    }

    function desfazer_preview() {
        if (state.previewCriado) {
            UndoRedoHandler.getInstance().undo();
            state.previewCriado = false;
            layer.invalidate();
        }
    }

    // UI
    const main_panel = new JPanel();
    main_panel.setLayout(new BoxLayout(main_panel, 1)); // Y_AXIS
    main_panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    // Info: vértices e orientação
    function texto_info() {
        const n_ext = vertex_convex_mut.filter(function(v) { return v; }).length;
        const n_int = vertex_convex_mut.filter(function(v) { return !v; }).length;
        return "<html><b>" + n + " vértices</b> — " + n_ext + " externos, " + n_int + " internos" +
               (raw_n > n ? " <font color='gray'>(" + (raw_n - n) + " ignorados)</font>" : "") +
               "<br><font color='#ffff00'><i>Use o botão inverter orientação caso<br>as cavidades estiverem ao contrário.</i></font></html>";
    }
    const lbl_info = new JLabel(texto_info());
    lbl_info.setFont(new Font("Dialog", 0, 12));
    lbl_info.setAlignmentX(0.0);
    main_panel.add(lbl_info);
    main_panel.add(Box.createVerticalStrut(6));

    // Controles com GridBagLayout
    const ctrl_panel = new JPanel(new GridBagLayout());
    ctrl_panel.setBorder(BorderFactory.createTitledBorder("Parâmetros"));
    ctrl_panel.setAlignmentX(0.0);

    const gbc = new GridBagConstraints();
    gbc.insets = new Insets(3, 5, 3, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL; // = 2

    const lbl_raio = new JLabel("Raio: " + state.radius.toFixed(2) + " m");
    const lbl_segs = new JLabel("Segmentos: " + state.segments);

    function atualizar_labels() {
        lbl_raio.setText("Raio: " + Math.min(state.radius, max_radius * 0.99).toFixed(2) + " m");
        lbl_segs.setText("Segmentos: " + state.segments);
    }

    function add_row(lbl, row, on_minus, on_plus) {
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 1.0;
        ctrl_panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0;
        const bM = new JButton("-");
        bM.setPreferredSize(new Dimension(40, 24));
        bM.addActionListener(new ActionListener({ actionPerformed: function() {
            on_minus(); atualizar_labels(); desenhar_preview();
        }}));
        ctrl_panel.add(bM, gbc);

        gbc.gridx = 2;
        const bP = new JButton("+");
        bP.setPreferredSize(new Dimension(40, 24));
        bP.addActionListener(new ActionListener({ actionPerformed: function() {
            on_plus(); atualizar_labels(); desenhar_preview();
        }}));
        ctrl_panel.add(bP, gbc);
    }

    // Passo do raio: ~2% do raio máximo, arredondado para 1 casa significativa
    // Ex: max_radius=50m → passo≈1.0m | max_radius=5m → passo≈0.1m
    const passo_raio = Math.max(0.05, parseFloat((max_radius * 0.02).toPrecision(1)));

    add_row(lbl_raio, 0,
        function() { state.radius = Math.max(passo_raio, state.radius - passo_raio); },
        function() { state.radius = Math.min(state.radius + passo_raio, max_radius); }
    );
    add_row(lbl_segs, 1,
        function() { state.segments = Math.max(1, state.segments - 1); },
        function() { state.segments++; }
    );

    main_panel.add(ctrl_panel);
    main_panel.add(Box.createVerticalStrut(4));

    // Botões: Inverter Orientação + Resetar
    const btn_aux_panel = new JPanel(new FlowLayout(2, 8, 2)); // CENTER
    btn_aux_panel.setAlignmentX(0.0);

    const btn_inverter = new JButton("⇄ Inverter Orientação");
    btn_inverter.addActionListener(new ActionListener({ actionPerformed: function() {
        orig_pts_filt = orig_pts_filt.slice().reverse();
        recalcular_convex();
        lbl_info.setText(texto_info());
        desenhar_preview();
    }}));
    btn_aux_panel.add(btn_inverter);

    const btn_reset = new JButton("Resetar");
    btn_reset.addActionListener(new ActionListener({ actionPerformed: function() {
        state.radius   = raio_inicial;
        state.segments = 4;
        atualizar_labels();
        desenhar_preview();
    }}));
    btn_aux_panel.add(btn_reset);

    main_panel.add(btn_aux_panel);
    main_panel.add(Box.createVerticalStrut(4));

    // OK / Cancelar
    const btn_panel = new JPanel(new FlowLayout(2, 8, 4));
    btn_panel.setAlignmentX(0.0);
    const btn_ok  = new JButton("OK",       UIManager.getIcon("OptionPane.yesIcon"));
    const btn_can = new JButton("Cancelar", UIManager.getIcon("OptionPane.noIcon"));
    btn_panel.add(btn_ok);
    btn_panel.add(btn_can);
    main_panel.add(btn_panel);

    // Diálogo
    const dialog = new JDialog(MainApplication.getMainFrame(), "Arredondar Polígono", false);
    dialog.setContentPane(main_panel);
    dialog.setDefaultCloseOperation(2);
    dialog.pack();
    dialog.setLocationRelativeTo(MainApplication.getMainFrame());

    // Layer listener
    const layerListener = new LayerChangeListener({
        layerAdded:        function(e) {},
        layerOrderChanged: function(e) {},
        layerRemoving:     function(e) {
            if (e.getRemovedLayer() === layer) {
                SwingUtilities.invokeLater(function() {
                    MainApplication.getLayerManager().removeLayerChangeListener(layerListener);
                    dialog.dispose();
                    new Notification("Camada removida. Fechando diálogo.")
                        .setIcon(UIManager.getIcon("OptionPane.warningIcon")).show();
                });
            }
        }
    });
    MainApplication.getLayerManager().addLayerChangeListener(layerListener);

    // Listeners
    btn_ok.addActionListener(new ActionListener({ actionPerformed: function() {
        MainApplication.getLayerManager().removeLayerChangeListener(layerListener);
        dialog.dispose();
        new Notification("Polígono arredondado criado com sucesso.")
            .setIcon(UIManager.getIcon("OptionPane.informationIcon")).show();
    }}));

    btn_can.addActionListener(new ActionListener({ actionPerformed: function() {
        MainApplication.getLayerManager().removeLayerChangeListener(layerListener);
        dialog.dispose();
        desfazer_preview();
        new Notification("Operação cancelada.")
            .setIcon(UIManager.getIcon("OptionPane.warningIcon")).show();
    }}));

    dialog.addWindowListener(new WindowAdapter({ windowClosing: function() {
        MainApplication.getLayerManager().removeLayerChangeListener(layerListener);
        desfazer_preview();
    }}));

    // Cria preview inicial e abre
    desenhar_preview();
    dialog.setVisible(true);
}
